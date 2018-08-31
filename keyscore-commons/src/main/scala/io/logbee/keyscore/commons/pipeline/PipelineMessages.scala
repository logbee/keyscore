package io.logbee.keyscore.commons.pipeline

import akka.actor.ActorRef
import io.logbee.keyscore.model.PipelineInstance
import io.logbee.keyscore.model.blueprint.PipelineBlueprint

case class PipelineBlueprintsResponse(pipelineBlueprints: List[PipelineBlueprint])
case class PipelineInstanceResponse(pipelineInstances: List[PipelineInstance])
case class RequestPipelineInstance(receiver: ActorRef)
case class RequestPipelineBlueprints(receiver: ActorRef)
