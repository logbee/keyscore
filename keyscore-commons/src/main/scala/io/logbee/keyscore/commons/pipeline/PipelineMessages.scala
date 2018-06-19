package io.logbee.keyscore.commons.pipeline
import akka.actor.ActorRef
import io.logbee.keyscore.model.PipelineInstance


case class PipelineInstanceResponse(pipelineInstances: List[PipelineInstance])
case class RequestPipelineInstance(receiver: ActorRef)