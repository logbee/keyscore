package io.logbee.keyscore.agent

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.agent.Agent.{CheckJoin, SendJoin}
import io.logbee.keyscore.commons.cluster.{AgentJoin, AgentJoinAccepted, AgentJoinFailure}
import io.logbee.keyscore.commons.util.RandomNameGenerator

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Agent {

  private case object SendJoin
  private case object CheckJoin
}

class Agent extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher

  val cluster = Cluster(context.system)
  val scheduler = context.system.scheduler
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  val name: String = new RandomNameGenerator("/agents.txt").nextName()
  var joined: Boolean = false

  override def preStart(): Unit = {
    scheduler.scheduleOnce(1 second) {
      self ! SendJoin
    }
    mediator ! Subscribe("agents", self)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe("agents", self)
  }

  override def receive: Receive = {
    case SendJoin =>
      mediator ! Publish("agents", AgentJoin(name))
      scheduler.scheduleOnce(5 seconds) {
        self ! CheckJoin
      }
    case AgentJoinAccepted() =>
      joined = true
      log.info("Agent joined")
    case AgentJoinFailure =>
      log.error("Agent join failed")
    case CheckJoin =>
      if (!joined) {
        self ! SendJoin
      }
  }
}
