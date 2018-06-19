package io.logbee.keyscore.commons.pipeline
import akka.actor.ActorRef
import io.logbee.keyscore.model.PipelineState


case class PipelineStateResponse(pipelineStates: List[PipelineState])
case class RequestPipelineState(receiver: ActorRef)