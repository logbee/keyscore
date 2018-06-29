package io.logbee.keyscore.commons.pipeline
import akka.actor.ActorRef
import io.logbee.keyscore.model.{PipelineConfiguration, PipelineInstance}

case class PipelineConfigurationResponse(pipelineConfigurations: List[PipelineConfiguration])
case class PipelineInstanceResponse(pipelineInstances: List[PipelineInstance])
case class RequestPipelineInstance(receiver: ActorRef)
case class RequestPipelineConfigurations(receiver: ActorRef)
