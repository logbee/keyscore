package io.logbee.keyscore.agent

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.Agent.{CheckJoin, ClusterAgentManagerDied, Initialize, SendJoin}
import io.logbee.keyscore.agent.pipeline.FilterManager.{DescriptorsResponse, RequestDescriptors}
import io.logbee.keyscore.agent.pipeline.{FilterManager, LocalPipelineManager}
import io.logbee.keyscore.commons.cluster.Topics.{AgentsTopic, ClusterTopic}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.extension.ExtensionLoader
import io.logbee.keyscore.commons.extension.ExtensionLoader.LoadExtensions
import io.logbee.keyscore.commons.util.StartUpWatch
import io.logbee.keyscore.commons.util.StartUpWatch.StartUpComplete

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Agent {

  case object Initialize

  private case object SendJoin
  private case object CheckJoin

  private case object ClusterAgentManagerDied

  def apply(id: UUID, name: String): Props = Props(new Agent(id, name))
}

/**
  * The '''Agent''' is the main Actor in the keyscore-agent package. <br><br>
  * On Startup, the Agent tries to join the `Cluster` and loads his `Extensions`. <br>
  * If the Join was accepted, he publishes his `Capabilites` to the cluster. <br><br>
  * The Agent creates: <br>
  *   * [[io.logbee.keyscore.agent.pipeline.FilterManager]] <br>
  *   * [[io.logbee.keyscore.agent.pipeline.LocalPipelineManager]] <br>
  *   * [[io.logbee.keyscore.commons.extension.ExtensionLoader]] <br>
  */
class Agent(id: UUID, name: String) extends Actor with ActorLogging {

  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val timeout: Timeout = 30 seconds

  //Agent properties
  private var joined: Boolean = false

  //Cluster
  private val cluster = Cluster(context.system)
  private val mediator = DistributedPubSub(context.system).mediator

  //System
  private val config = ConfigFactory.load()
  private val scheduler = context.system.scheduler

  //Necessary Actors for the whole keyscore-agent system
  private val filterManager = context.actorOf(Props[FilterManager], "filter-manager")
  private val localPipelineManager = context.actorOf(LocalPipelineManager(filterManager), "LocalPipelineManager")
  private val extensionLoader = context.actorOf(Props[ExtensionLoader], "extension-loader")

  override def preStart(): Unit = {
    log.info(s"The Agent '$name' has started.")
    Cluster(context.system) registerOnMemberUp {
      scheduler.scheduleOnce(5 second) {
        self ! SendJoin
      }
    }
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Subscribe(ClusterTopic, self)
    mediator ! Publish(ClusterTopic, ActorJoin(Roles.AgentRole, self))
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, ActorLeave(Roles.AgentRole, self))
    mediator ! Unsubscribe(AgentsTopic, self)
    mediator ! Unsubscribe(ClusterTopic, self)
    log.info(s"The Agent '$name' has stopped.")
  }

  override def receive: Receive = {
    case Initialize =>
      log.debug("Initializing Agent with FilterManager ...")
      val startUpWatch = context.actorOf(StartUpWatch(filterManager))
      (startUpWatch ? StartUpComplete).onComplete {
        case Success(_) =>
          log.debug("Initializing Agent completed.")
          extensionLoader ! LoadExtensions(config, Paths.ExtensionPath)
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

  }
}
