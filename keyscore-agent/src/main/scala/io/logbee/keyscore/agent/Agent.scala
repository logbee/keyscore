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
import io.logbee.keyscore.commons.util.StartUpWatch.StartUpComplete
import io.logbee.keyscore.commons.util.{RandomNameGenerator, StartUpWatch}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Agent {
  case object Initialize

  private case object SendJoin
  private case object CheckJoin

  private case object ClusterAgentManagerDied
}

class Agent extends Actor with ActorLogging {

  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val timeout: Timeout = 30 seconds

  private val config = ConfigFactory.load()
  private val cluster = Cluster(context.system)
  private val scheduler = context.system.scheduler

  private val mediator = DistributedPubSub(context.system).mediator
  private val filterManager = context.actorOf(Props[FilterManager], "filter-manager")
  private val localPipelineManager = context.actorOf(LocalPipelineManager(filterManager), "LocalPipelineManager")
  private val extensionLoader = context.actorOf(Props[ExtensionLoader], "extension-loader")

  private val name: String = new RandomNameGenerator("/agents.txt").nextName()
  private var joined: Boolean = false

  override def preStart(): Unit = {
    log.info(s"Agent ${name} started.")
    Cluster(context.system) registerOnMemberUp {
      scheduler.scheduleOnce(5 second) {
        self ! SendJoin
      }
    }
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Subscribe(ClusterTopic, self)
    mediator ! Publish(ClusterTopic, ActorJoin("Agent", self))
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, ActorLeave("Agent", self))
    mediator ! Unsubscribe(AgentsTopic, self)
    mediator ! Unsubscribe(ClusterTopic, self)
    log.info(s"Agent ${name} stopped.")
  }

  override def receive: Receive = {
    case Initialize =>
      val currentSender = sender
      val startUpWatch = context.actorOf(StartUpWatch(filterManager))
      (startUpWatch ? StartUpComplete).onComplete {
        case Success(_) =>
          extensionLoader ! LoadExtensions(config, "keyscore.agent.extensions")
        case Failure(e) =>
          log.error(e, "Failed to initialize agent!")
          context.stop(self)
      }

    case SendJoin =>
      val agentJoin = AgentJoin(UUID.randomUUID(), name)
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
          log.info("Published capabilities to the topic agents.")
        case Failure(e) =>
          log.error(e, "Failed to publish capabilities!")
          context.stop(self)
      }
      context.watchWith(sender, ClusterAgentManagerDied)

    case AgentJoinFailure =>
      log.error("Agent join failed")
      context.stop(self)

    case ClusterAgentManagerDied =>
      log.info("Actual ClusterAgentManager diededed. Setting joined-status to false and trying to join again.")
      joined = false
      self ! SendJoin

  }
}
