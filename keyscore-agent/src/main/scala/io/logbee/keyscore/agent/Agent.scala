package io.logbee.keyscore.agent

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.Agent.{CheckJoin, Initialize, SendJoin}
import io.logbee.keyscore.agent.FilterManager.{Descriptors, GetDescriptors}
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, AgentJoin, AgentJoinAccepted, AgentJoinFailure}
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
}

class Agent extends Actor with ActorLogging {

  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val timeout: Timeout = 30 seconds

  private val config = ConfigFactory.load()
  private val cluster = Cluster(context.system)
  private val scheduler = context.system.scheduler

  private val mediator = DistributedPubSub(context.system).mediator
  private val filterManager = context.actorOf(Props[FilterManager], "filter-manager")
  private val extensionLoader = context.actorOf(Props[ExtensionLoader], "extension-loader")

  private val name: String = new RandomNameGenerator("/agents.txt").nextName()
  private var joined: Boolean = false

  override def preStart(): Unit = {
    Cluster(context.system) registerOnMemberUp {
      scheduler.scheduleOnce(5 second) {
        self ! SendJoin
      }
    }
    mediator ! Subscribe("agents", self)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe("agents", self)
  }

  override def receive: Receive = {
    case Initialize =>
      implicit val startUpTimeout: Timeout = 30 seconds
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
      mediator ! Publish("agents", AgentJoin(name))
      scheduler.scheduleOnce(5 seconds) {
        self ! CheckJoin
      }
    case CheckJoin =>
      if (!joined) {
        self ! SendJoin
      }
    case AgentJoinAccepted() =>
      log.info("Agent joined")
      joined = true
      (filterManager ? GetDescriptors).mapTo[Descriptors].onComplete {
        case Success(message) =>
          mediator ! Publish("agents", AgentCapabilities(message.descriptors))
        case Failure(e) =>
          log.error(e, "Failed to publish capabilities!")
      }
    case AgentJoinFailure =>
      log.error("Agent join failed")

  }
}
