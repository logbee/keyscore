package io.logbee.keyscore.frontier.cluster.pipeline.collectors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetBlueprintRequest, GetBlueprintResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.BlueprintCollector.{BlueprintsCollectorResponse, BlueprintsCollectorResponseFailure, CheckForBlueprintList}
import io.logbee.keyscore.model.blueprint.{PipelineBlueprint, SealedBlueprint}

import scala.concurrent.duration._

/**
  * Returns a List of SealedBlueprints from a list of BlueprintRefs after collecting them from the BlueprintManager.
  */
object BlueprintCollector {
  def apply(pipelineBlueprint: PipelineBlueprint, blueprintManager: ActorRef) = Props(new BlueprintCollector(pipelineBlueprint, blueprintManager))

  case class BlueprintsCollectorResponse(blueprints: List[SealedBlueprint])

  case object BlueprintsCollectorResponseFailure

  private case object CheckForBlueprintList

}

class BlueprintCollector(pipelineBlueprint: PipelineBlueprint, blueprintManager: ActorRef) extends Actor with ActorLogging {

  import context.{dispatcher, system}

  private var blueprints = scala.collection.mutable.ListBuffer.empty[SealedBlueprint]

  override def preStart(): Unit = {
    log.debug(" started.")
    pipelineBlueprint.blueprints.foreach(current => {
      blueprintManager ! GetBlueprintRequest(current)
    })
    system.scheduler.scheduleOnce(5 seconds) {
      self ! CheckForBlueprintList
    }
  }

  override def receive: Receive = {
    case GetBlueprintResponse(current) => current match {
      case Some(blueprint) =>
        blueprints += blueprint
        if (blueprints.size == pipelineBlueprint.blueprints.size) {
          context.parent ! BlueprintsCollectorResponse(blueprints.toList)
          context.stop(self)
        }
      case _ =>
    }
    case CheckForBlueprintList =>
      if (blueprints.size != pipelineBlueprint.blueprints.size) {
        context.parent ! BlueprintsCollectorResponseFailure
        context.stop(self)
      }
  }
}
