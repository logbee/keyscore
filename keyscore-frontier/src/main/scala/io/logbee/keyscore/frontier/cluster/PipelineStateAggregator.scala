package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.commons.pipeline.PipelineStateResponse
import io.logbee.keyscore.model.PipelineState

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object PipelineStateAggregator {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new PipelineStateAggregator(receiver, children))
}

class PipelineStateAggregator(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor {

  import context.{dispatcher, system}

  private var states = mutable.ListBuffer.empty[PipelineState]

  override def preStart(): Unit = {
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! PipelineStateResponse(states.toList)
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case state: PipelineState =>
      states += state
  }
}
