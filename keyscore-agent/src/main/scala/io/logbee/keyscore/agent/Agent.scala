package io.logbee.keyscore.agent

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import io.logbee.keyscore.agent.Agent.SendJoin
import io.logbee.keyscore.cluster.AgentJoin

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Agent {

  case object SendJoin
}

class Agent extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher
  val cluster = Cluster(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    context.system.scheduler.scheduleOnce(500 milliseconds) {
      self ! SendJoin
    }
  }

  override def postStop(): Unit = {
  }

  override def receive: Receive = {
    case SendJoin =>
      mediator ! Publish("agents", AgentJoin("Elmar"))
  }
}
