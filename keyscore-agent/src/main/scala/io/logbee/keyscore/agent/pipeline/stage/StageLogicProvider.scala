package io.logbee.keyscore.agent.pipeline.stage

import akka.actor.typed.ActorRef
import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{BranchStage, FilterStage, MergeStage, SinkStage, SourceStage}

object StageLogicProvider {

  trait StageLogicProviderRequest

  trait StageLogicProviderResponse

  case class Load(replyTo: ActorRef[StageLogicProviderResponse]) extends StageLogicProviderRequest

  case class LoadSuccess(descriptors: List[Descriptor], replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse

  case class CreateSourceStage(ref: DescriptorRef, parameters: LogicParameters, replyTo: ActorRef[StageLogicProviderResponse]) extends StageLogicProviderRequest
  case class SourceStageCreated(ref: DescriptorRef, stage: SourceStage, replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse

  case class CreateFilterStage(ref: DescriptorRef, parameters: LogicParameters, replyTo: ActorRef[StageLogicProviderResponse]) extends StageLogicProviderRequest
  case class FilterStageCreated(ref: DescriptorRef, stage: FilterStage, replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse

  case class CreateSinkStage(ref: DescriptorRef, parameters: LogicParameters, replyTo: ActorRef[StageLogicProviderResponse]) extends StageLogicProviderRequest
  case class SinkStageCreated(ref: DescriptorRef, stage: SinkStage, replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse

  case class CreateBranchStage(ref: DescriptorRef, parameters: LogicParameters, replyTo: ActorRef[StageLogicProviderResponse]) extends StageLogicProviderRequest
  case class BranchStageCreated(ref: DescriptorRef, stage: BranchStage, replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse

  case class CreateMergeStage(ref: DescriptorRef, parameters: LogicParameters, replyTo: ActorRef[StageLogicProviderResponse]) extends StageLogicProviderRequest
  case class MergeStageCreated(ref: DescriptorRef, stage: MergeStage, replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse

  case class UninitializedFailure(message: String) extends StageLogicProviderResponse

  case class DescriptorNotFound(descriptorRef: DescriptorRef, blueprintRef: BlueprintRef, replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse

  case class StageCreationFailed(descriptorRef: DescriptorRef, blueprintRef: BlueprintRef, replyTo: ActorRef[StageLogicProviderRequest]) extends StageLogicProviderResponse
}
