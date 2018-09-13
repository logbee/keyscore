package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages._
import io.logbee.keyscore.commons.{BlueprintService, HereIam, WhoIs}
import io.logbee.keyscore.model.blueprint.ToBase.sealedToBase
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}

/**
  * The BlueprintManager holds maps for all Pipeline- and SealedBlueprints and <br>
  * resolves a BlueprintRef to the specific Blueprint.
  */
object BlueprintManager {

  def apply(): Props = Props(new BlueprintManager())
}

class BlueprintManager extends Actor with ActorLogging {

  private val pipelineBlueprints = scala.collection.mutable.Map.empty[BlueprintRef, PipelineBlueprint]
  private val blueprints = scala.collection.mutable.Map.empty[BlueprintRef, SealedBlueprint]

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    log.info("BlueprintManager started.")
    mediator ! Subscribe(Topics.WhoIsTopic, self)
  }

  override def postStop(): Unit = {
    log.info("BlueprintManager stopped.")
  }

  override def receive: Receive = {

    case WhoIs(BlueprintService) =>
      sender ! HereIam(BlueprintService, self)

    //Pipeline Blueprint
    case StorePipelineBlueprintRequest(pipelineBlueprint) =>
      pipelineBlueprints.put(pipelineBlueprint.ref, pipelineBlueprint)
      sender ! StorePipelineBlueprintResponse

    case UpdatePipelineBlueprintRequest(pipelineBlueprint) =>
      if(pipelineBlueprints.contains(pipelineBlueprint.ref)){
        pipelineBlueprints.put(pipelineBlueprint.ref, pipelineBlueprint)
        sender ! UpdatePipelineBlueprintResponseSuccess
      } else {
        sender ! UpdatePipelineBlueprintResponseFailure
      }

    case GetAllPipelineBlueprintsRequest =>
      sender ! GetAllPipelineBlueprintsResponse(pipelineBlueprints.toMap)

    case GetPipelineBlueprintRequest(ref) =>
      sender ! GetPipelineBlueprintResponse(pipelineBlueprints.get(ref))

    case DeletePipelineBlueprintRequest(ref) =>
      pipelineBlueprints.remove(ref)
      sender ! DeletePipelineBlueprintResponse

    //Sealed Blueprint
    case StoreBlueprintRequest(blueprint) =>
      blueprints.put(blueprint.blueprintRef, blueprint)
      sender ! StoreBlueprintResponse

    case UpdateBlueprintRequest(blueprint) =>
      if(blueprints.contains(blueprint.blueprintRef)){
        blueprints.put(blueprint.blueprintRef, blueprint)
        sender ! UpdateBlueprintResponseSuccess
      } else {
        sender ! UpdateBlueprintResponseFailure
      }

    case GetBlueprintRequest(ref) =>
      sender ! GetBlueprintResponse(blueprints.get(ref))

    case GetAllBlueprintsRequest =>
      sender ! GetAllBlueprintsResponse(blueprints.toMap)

    case DeleteBlueprintRequest(ref) =>
      blueprints.remove(ref)
      sender ! DeleteBlueprintResponse
  }
}
