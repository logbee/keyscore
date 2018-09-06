package io.logbee.keyscore.commons.cluster.resources

import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}

object BlueprintMessages {

  case class StorePipelineBlueprintRequest(pipelineBlueprint: PipelineBlueprint)
  case class StoreBlueprintRequest(blueprint: SealedBlueprint)

  case class DeletePipelineBlueprintsRequest(ref: BlueprintRef)
  case class DeleteBlueprintRequest(ref: BlueprintRef)

  case object GetAllPipelineBlueprintsRequest
  case class GetAllPipelineBlueprintsResponse(blueprints: List[PipelineBlueprint])

  case object GetAllBlueprintsRequest
  case class GetAllBlueprintsResponse(blueprints: List[SealedBlueprint])

  case class GetPipelineBlueprintRequest(ref: BlueprintRef)
  case class GetPipelineBlueprintResponse(pipelineBlueprint: Option[PipelineBlueprint])

  case class GetBlueprintRequest(ref: BlueprintRef)
  case class GetBlueprintResponse(blueprint: Option[SealedBlueprint])

}
