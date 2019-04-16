package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.commons.pipeline.PipelineBlueprintsResponse
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.PipelineBlueprintCollector.CheckPipelineBlueprints
import io.logbee.keyscore.model.blueprint.PipelineBlueprint

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * Returns a list of Blueprints.
  */
object PipelineBlueprintCollector {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new PipelineBlueprintCollector(receiver, children))

  case object PipelineBlueprintsResponseFailure
  private case object CheckPipelineBlueprints
}

class PipelineBlueprintCollector(receiver: ActorRef, children: Iterable[ActorRef], timeout: FiniteDuration = 5 seconds) extends Actor with ActorLogging {
  import context.{dispatcher, system}

  private var blueprints = mutable.ListBuffer.empty[PipelineBlueprint]

  override def preStart(): Unit = {
    log.debug(s" started")
    system.scheduler.scheduleOnce(timeout, self, CheckPipelineBlueprints)
  }

  override def receive: Receive = {
    case pipelineBlueprint: PipelineBlueprint =>
      blueprints += pipelineBlueprint

    case CheckPipelineBlueprints =>
      receiver ! PipelineBlueprintsResponse(blueprints.toList)
      context.stop(self)

  }

}