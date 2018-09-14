package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.commons.pipeline.PipelineBlueprintsResponse
import io.logbee.keyscore.model.blueprint.PipelineBlueprint

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Returns a list of Blueprints.
  */
object PipelineBlueprintCollector {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new PipelineBlueprintCollector(receiver, children))
}

class PipelineBlueprintCollector(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor with ActorLogging {
  import context.{dispatcher, system}

  private var blueprints = mutable.ListBuffer.empty[PipelineBlueprint]

  override def preStart(): Unit = {
    log.info(s"PipelineBlueprintCollector started")
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! PipelineBlueprintsResponse(blueprints.toList)
      log.info(s"PipelineBlueprintCollector stopped")
      context.stop(self)
    }
  }


  override def receive: Receive = {
    case pipelineBlueprint: PipelineBlueprint =>
      log.info("PipelineBlueprintCollector received PipelineBlueprint")
      blueprints += pipelineBlueprint
  }

}