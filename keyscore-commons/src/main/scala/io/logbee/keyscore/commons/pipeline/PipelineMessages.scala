package io.logbee.keyscore.commons.pipeline

import java.util.UUID

import akka.actor.ActorRef
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.blueprint.PipelineBlueprint

case class RequestPipelineBlueprints(receiver: ActorRef)
case class PipelineBlueprintsResponse(pipelineBlueprints: List[PipelineBlueprint])

case object RequestPipelineInstance
case class PipelineInstanceResponse(pipelineInstances: List[PipelineInstance])

case class PipelineMaterialized(uuid: UUID)

case class PipelineRemoved(uuid: UUID)