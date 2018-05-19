package io.logbee.keyscore.agent.stream

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.model.StreamConfiguration

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object StreamConfigurationAggregator {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new StreamConfigurationAggregator(receiver, children))
}

class StreamConfigurationAggregator(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor {

  import context.{dispatcher, system}

  private var configurations = mutable.ListBuffer.empty[StreamConfiguration]

  override def preStart(): Unit = {
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! configurations.toList
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case configuration: StreamConfiguration =>
      configurations += configuration
  }
}
