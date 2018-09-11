package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorRef, Props}
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetBlueprintResponse, GetPipelineBlueprintRequest}
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.PipelineDeployer.BlueprintsResponse
import io.logbee.keyscore.model.blueprint.ToBase.sealedToBase
import io.logbee.keyscore.model.blueprint.{PipelineBlueprint, SealedBlueprint}

import scala.concurrent.duration._


object BlueprintCollector {
  def apply(receiver: ActorRef, pipelineBlueprint: PipelineBlueprint, blueprintManager: ActorRef) = Props(new BlueprintCollector(receiver, pipelineBlueprint, blueprintManager))


}

class BlueprintCollector(receiver: ActorRef, pipelineBlueprint: PipelineBlueprint, blueprintManager: ActorRef) extends Actor {
  import context.{dispatcher, system}

  private var blueprints = scala.collection.mutable.ListBuffer.empty[SealedBlueprint]

  override def preStart(): Unit = {
    pipelineBlueprint.blueprints.foreach(current => {
      blueprintManager ! GetPipelineBlueprintRequest(current.blueprintRef)
    })
    system.scheduler.scheduleOnce(5 seconds) {
      receiver ! BlueprintsResponse(blueprints.toList)
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case  GetBlueprintResponse(current) => current match {
      case Some(blueprint) => blueprints += blueprint
      case _ =>
    }
  }
}
