package io.logbee.keyscore.commons.cluster.resources

import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}

object BlueprintMessages {

  // Pipeline Blueprints
  case class StorePipelineBlueprintRequest(pipelineBlueprint: PipelineBlueprint)
  case object StorePipelineBlueprintResponse

  case class UpdatePipelineBlueprintRequest(pipelineBlueprint: PipelineBlueprint)
  case object UpdatePipelineBlueprintResponseSuccess
  case object UpdatePipelineBlueprintResponseFailure

  case class GetPipelineBlueprintRequest(ref: BlueprintRef)
  case class GetPipelineBlueprintResponse(pipelineBlueprint: Option[PipelineBlueprint])

  case object GetAllPipelineBlueprintsRequest
  case class GetAllPipelineBlueprintsResponse(blueprints: Map[BlueprintRef, PipelineBlueprint])

  case class DeletePipelineBlueprintRequest(ref: BlueprintRef)
  case object DeletePipelineBlueprintResponse

  case object DeleteAllPipelineBlueprintsRequest
  case object DeleteAllPipelineBlueprintsResponse
  // Sealed Blueprints
  case class StoreBlueprintRequest(blueprint: SealedBlueprint)
  case object StoreBlueprintResponse

  case class UpdateBlueprintRequest(blueprint: SealedBlueprint)
  case object UpdateBlueprintResponseSuccess
  case object UpdateBlueprintResponseFailure

  case class GetBlueprintRequest(ref: BlueprintRef)
  case class GetBlueprintResponse(blueprint: Option[SealedBlueprint])

  case object GetAllBlueprintsRequest
  case class GetAllBlueprintsResponse(blueprints: Map[BlueprintRef, SealedBlueprint])

  case class DeleteBlueprintRequest(ref: BlueprintRef)
  case object DeleteBlueprintResponse
}
