package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.commons.pipeline.PipelineInstanceResponse
import io.logbee.keyscore.model.PipelineInstance

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Returns a list of PipelineInstances.
  */
object PipelineInstanceCollector {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new PipelineInstanceCollector(receiver, children))
}

class PipelineInstanceCollector(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor with ActorLogging {

  import context.{dispatcher, system}

  private var states = mutable.ListBuffer.empty[PipelineInstance]

  override def preStart(): Unit = {
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! PipelineInstanceResponse(states.toList)
      log.info("PipelineInstanceCollector returned states")
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case state: PipelineInstance =>
      log.info("PipelineInstanceCollector received PipelineInstance")
      states += state
  }
}
