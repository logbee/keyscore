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
  * The '''BlueprintManager''' holds maps for all `PipelineBlueprints` and `SealedBlueprints` and <br>
  * resolves a BlueprintRef to the specific Blueprint.
  *
  * @todo Error Handling
  */
object BlueprintManager {

  def apply(): Props = Props(new BlueprintManager())
}

class BlueprintManager extends Actor with ActorLogging {

  private val pipelineBlueprints = scala.collection.mutable.Map.empty[BlueprintRef, PipelineBlueprint]
  private val blueprints = scala.collection.mutable.Map.empty[BlueprintRef, SealedBlueprint]

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
    log.debug(" started.")
  }

  override def postStop(): Unit = {
    log.debug(" stopped.")
  }

  override def receive: Receive = {

    case WhoIs(BlueprintService) =>
      log.debug("Received WhoIs(BlueprintService)")
      sender ! HereIam(BlueprintService, self)

    case StoreOrUpdatePipelineBlueprintRequest(pipelineBlueprint) =>
      if (pipelineBlueprints.contains(pipelineBlueprint.ref)) {
        self forward UpdatePipelineBlueprintRequest(pipelineBlueprint)
      } else {
        self forward StorePipelineBlueprintRequest(pipelineBlueprint)
      }
    //Pipeline Blueprint
    case StorePipelineBlueprintRequest(pipelineBlueprint) =>
      log.debug(s"Received StorePipelineBlueprintRequest for $pipelineBlueprint")
      pipelineBlueprints.put(pipelineBlueprint.ref, pipelineBlueprint)
      sender ! StorePipelineBlueprintResponse

    case UpdatePipelineBlueprintRequest(pipelineBlueprint) =>
      log.debug(s"Received UpdatePipelineBlueprintRequest for $pipelineBlueprint")
      if (pipelineBlueprints.contains(pipelineBlueprint.ref)) {
        pipelineBlueprints.put(pipelineBlueprint.ref, pipelineBlueprint)
        log.debug(s"Updated PipelineBlueprint for $pipelineBlueprint")
        sender ! UpdatePipelineBlueprintResponseSuccess
      } else {
        log.warning(s"Couldn't update PipelineBlueprint for $pipelineBlueprint")
        sender ! UpdatePipelineBlueprintResponseFailure
      }

    case GetAllPipelineBlueprintsRequest =>
      log.debug(s"Received GetAllPipelineBlueprintsRequest")
      sender ! GetAllPipelineBlueprintsResponse(pipelineBlueprints.toMap)

    case GetPipelineBlueprintRequest(ref) =>
      log.debug(s"Received GetPipelineBlueprintRequest for <${ref.uuid}>")
      sender ! GetPipelineBlueprintResponse(pipelineBlueprints.get(ref))

    case DeletePipelineBlueprintRequest(ref) =>
      log.debug(s"Received DeletePipelineBlueprintRequest for <${ref.uuid}>")
      pipelineBlueprints.remove(ref)
      sender ! DeletePipelineBlueprintResponse

    case DeleteAllPipelineBlueprintsRequest =>
      log.debug("Received DeleteAllPipelineBlueprintsRequest")
      pipelineBlueprints.clear()
      sender ! DeleteAllPipelineBlueprintsResponse

    //Sealed Blueprint
    case StoreOrUpdateBlueprintRequest(blueprint) =>
      if (blueprints.contains(blueprint.blueprintRef)) {
        self forward UpdateBlueprintRequest(blueprint)
      } else {
        self forward StoreBlueprintRequest(blueprint)
      }

    case StoreBlueprintRequest(blueprint) =>
      log.debug(s"Received StoreBlueprintRequest for $blueprint")
      blueprints.put(blueprint.blueprintRef, blueprint)
      sender ! StoreBlueprintResponse

    case UpdateBlueprintRequest(blueprint) =>
      log.debug(s"Received UpdateBlueprintRequest for $blueprint")
      if (blueprints.contains(blueprint.blueprintRef)) {
        log.debug(s"Updated Blueprint for $blueprint")
        blueprints.put(blueprint.blueprintRef, blueprint)
        sender ! UpdateBlueprintResponseSuccess
      } else {
        log.warning(s"Couldn't update Blueprint for $blueprint")
        sender ! UpdateBlueprintResponseFailure
      }

    case GetBlueprintRequest(ref) =>
      log.debug(s"Received GetBlueprintRequest for <${ref.uuid}>")
      sender ! GetBlueprintResponse(blueprints.get(ref))

    case GetAllBlueprintsRequest =>
      log.debug("Received GetAllBlueprintsRequest")
      sender ! GetAllBlueprintsResponse(blueprints.toMap)

    case DeleteBlueprintRequest(ref) =>
      log.debug(s"Received DeleteBlueprintRequest for <${ref.uuid}>")
      blueprints.remove(ref)
      sender ! DeleteBlueprintResponse

    case DeleteAllBlueprintsRequest =>
      log.debug("Received DeleteAllBlueprintsRequest")
      blueprints.clear()
      sender ! DeleteAllBlueprintsResponse
  }
}
