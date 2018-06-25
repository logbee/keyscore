package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.commons.pipeline.PipelineInstanceResponse
import io.logbee.keyscore.model.PipelineInstance

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object PipelineInstanceCollector {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new PipelineInstanceCollector(receiver, children))
}

class PipelineInstanceCollector(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor {

  import context.{dispatcher, system}

  private var states = mutable.ListBuffer.empty[PipelineInstance]

  override def preStart(): Unit = {
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! PipelineInstanceResponse(states.toList)
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case state: PipelineInstance =>
      states += state
  }
}
