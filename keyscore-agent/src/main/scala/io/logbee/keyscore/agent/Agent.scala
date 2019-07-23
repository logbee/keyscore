package io.logbee.keyscore.agent

import java.lang.management.ManagementFactory
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.pattern.ask
import akka.util.Timeout
import com.sun.management.OperatingSystemMXBean
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.pipeline.FilterManager.{DescriptorsResponse, RequestDescriptors}
import io.logbee.keyscore.agent.pipeline.stage.ManifestStageLogicProvider
import io.logbee.keyscore.agent.pipeline.stage.StageLogicProvider.StageLogicProviderRequest
import io.logbee.keyscore.agent.pipeline.{FilterManager, LocalPipelineManager}
import io.logbee.keyscore.commons.cluster.Topics.{AgentsTopic, ClusterTopic, MetricsTopic}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.metrics.{ScrapeMetrics, ScrapeMetricsSuccess}
import io.logbee.keyscore.commons.util.StartUpWatch
import io.logbee.keyscore.commons.util.StartUpWatch.StartUpComplete
import io.logbee.keyscore.model.data.Importance.High
import io.logbee.keyscore.model.localization.TextRef
import io.logbee.keyscore.model.metrics.{GaugeMetricDescriptor, MetricsCollection}
import io.logbee.keyscore.pipeline.api.metrics.DefaultMetricsCollector

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Agent {

  case object Initialize

  private case object SendJoin
  private case object CheckJoin

  private case object ClusterAgentManagerDied

  val processUsedMemoryMetric = GaugeMetricDescriptor(
    name = "process_used_memory",
    displayName = TextRef("processUsedMemoryName"),
    description = TextRef("processUsedMemoryDesc"),
    importance = High
  )

  val systemUsedMemoryMetric = GaugeMetricDescriptor(
    name = "system_used_memory",
    displayName = TextRef("systemUsedMemoryName"),
    description = TextRef("systemUsedMemoryDesc"),
    importance = High
  )

  val freeMemoryMetric = GaugeMetricDescriptor(
    name = "free_memory",
    displayName = TextRef("freeMemoryName"),
    description = TextRef("freeMemoryDesc"),
    importance = High
  )

  val processCpuLoadMetric = GaugeMetricDescriptor(
    name = "process_cpu_load",
    displayName = TextRef("processCpuLoadName"),
    description = TextRef("processCpuLoadDesc"),
    importance = High,
  )

  val systemCpuLoadMetric = GaugeMetricDescriptor(
    name = "system_cpu_load",
    displayName = TextRef("systemCpuLoadName"),
    description = TextRef("systemCpuLoadDesc"),
    importance = High
  )


  def apply(id: UUID, name: String): Props = Props(new Agent(id, name))
}

/**
  * The '''Agent''' is the main Actor in the keyscore-agent package. <br><br>
  * On Startup, the Agent tries to join the `Cluster` and loads his `Extensions`. <br>
  * If the Join was accepted, he publishes his `Capabilites` to the cluster. <br><br>
  * The Agent creates: <br>
  *   * [[io.logbee.keyscore.agent.pipeline.FilterManager]] <br>
  *   * [[io.logbee.keyscore.agent.pipeline.LocalPipelineManager]]
  */
class Agent(id: UUID, name: String) extends Actor with ActorLogging {

  import Agent._
  import akka.actor.typed.scaladsl.adapter._

  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val timeout: Timeout = 30 seconds

  //Agent properties
  private var joined: Boolean = false

  //Metrics
  private val metrics = new DefaultMetricsCollector()

  //Cluster
  Cluster(context.system)
  private val mediator = DistributedPubSub(context.system).mediator

  //System
  private val config = ConfigFactory.load()
  private val scheduler = context.system.scheduler

  //Necessary Actors for the whole keyscore-agent system
  private val stageLogicProviders: List[akka.actor.typed.ActorRef[StageLogicProviderRequest]] = List(
    context.actorOf(ManifestStageLogicProvider(), "manifest-stage-logic-provider")
  )

  private val filterManager = context.actorOf(FilterManager(stageLogicProviders), "filter-manager")

  context.actorOf(LocalPipelineManager(filterManager), "LocalPipelineManager")

  override def preStart(): Unit = {
    log.info(s"The Agent $name <$id> has started.")
    Cluster(context.system) registerOnMemberUp {
      scheduler.scheduleOnce(5 second) {
        self ! SendJoin
      }
    }
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Subscribe(ClusterTopic, self)
    mediator ! Subscribe(MetricsTopic, self)
    mediator ! Publish(ClusterTopic, ActorJoin(Roles.AgentRole, self))
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, ActorLeave(Roles.AgentRole, self))
    mediator ! Unsubscribe(AgentsTopic, self)
    mediator ! Unsubscribe(ClusterTopic, self)
    mediator ! Unsubscribe(MetricsTopic, self)
    log.info(s"The Agent $name <$id> has stopped.")
  }

  override def receive: Receive = {
    case Initialize =>
      log.debug("Initializing Agent with FilterManager ...")
      val startUpWatch = context.actorOf(StartUpWatch(filterManager))
      (startUpWatch ? StartUpComplete).onComplete {
        case Success(_) =>
          log.debug("Initializing Agent completed.")
        case Failure(e) =>
          log.error(e, "Failed to initialize agent!")
          context.stop(self)
      }

    case SendJoin =>
      val agentJoin = AgentJoin(id, name)
      log.info(s"Trying to join cluster: $agentJoin")
      mediator ! Publish(AgentsTopic, agentJoin)
      scheduler.scheduleOnce(10 seconds) {
        self ! CheckJoin
      }

    case CheckJoin =>
      if (!joined) {
        self ! SendJoin
      }

    case AgentJoinAccepted() =>
      log.info("Joined cluster successfully.")
      joined = true
      (filterManager ? RequestDescriptors).mapTo[DescriptorsResponse].onComplete {
        case Success(message) =>
          mediator ! Publish(AgentsTopic, AgentCapabilities(message.descriptors))
          log.info(s"Published $name's capabilities to the topic agents.")
        case Failure(e) =>
          log.error(e, s"Failed to publish $name's capabilities!")
          context.stop(self)
      }
      context.watchWith(sender, ClusterAgentManagerDied)

    case AgentJoinFailure =>
      log.error(s"Agent $name's join failed")
      context.stop(self)

    case ClusterAgentManagerDied =>
      log.info("Actual ClusterAgentManager diededed. Setting joined-status to false and trying to join again.")
      joined = false
      self ! SendJoin

    case ScrapeMetrics(manager) =>
      val collection = computeMetrics()
      val map = Map(id.toString -> collection)
      manager ! ScrapeMetricsSuccess(map)
  }

  private def computeMetrics(): MetricsCollection = {

    val runtime = Runtime.getRuntime
    val osBean = ManagementFactory.getPlatformMXBean(classOf[OperatingSystemMXBean])

    val physicalTotalMemory = osBean.getTotalPhysicalMemorySize
    val jvmTotalMemory = runtime.totalMemory()

    metrics.collect(processUsedMemoryMetric)
      .set(jvmTotalMemory - runtime.freeMemory())
      .min(0)
      .max(runtime.maxMemory())

    metrics.collect(systemUsedMemoryMetric)
      .set(physicalTotalMemory - osBean.getFreePhysicalMemorySize)
      .min(0)
      .max(physicalTotalMemory)

    metrics.collect(processCpuLoadMetric).set(osBean.getProcessCpuLoad).min(0).max(1)
    metrics.collect(systemCpuLoadMetric).set(osBean.getSystemCpuLoad).min(0).max(1)

    metrics.get
  }
}
