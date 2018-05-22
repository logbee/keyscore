package io.logbee.keyscore.agent.stream

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.model.StreamState

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object StreamStateAggregator {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new StreamStateAggregator(receiver, children))
}

class StreamStateAggregator(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor {

  import context.{dispatcher, system}

  private var models = mutable.ListBuffer.empty[StreamState]

  override def preStart(): Unit = {
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! models.toList
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case model: StreamState =>
      models += model
  }
}
