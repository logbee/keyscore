package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.commons.pipeline.PipelineBlueprintsResponse
import io.logbee.keyscore.model.blueprint.PipelineBlueprint

import scala.collection.mutable
import scala.concurrent.duration._

object PipelineConfigurationCollector {
  def apply(receiver: ActorRef, children: Iterable[ActorRef]) = Props(new PipelineConfigurationCollector(receiver, children))
}

class PipelineConfigurationCollector(receiver: ActorRef, children: Iterable[ActorRef]) extends Actor {
  import context.{dispatcher, system}

  private var blueprints = mutable.ListBuffer.empty[PipelineBlueprint]

  override def preStart(): Unit = {
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! PipelineBlueprintsResponse(blueprints.toList)
      context.stop(self)
    }
  }


  override def receive: Receive = {
    case pipelineBlueprint: PipelineBlueprint =>
      blueprints += pipelineBlueprint
  }

}