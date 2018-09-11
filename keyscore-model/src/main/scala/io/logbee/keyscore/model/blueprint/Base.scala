package io.logbee.keyscore.model.blueprint

import io.logbee.keyscore.model.descriptor.DescriptorRef

object ToBase {
  implicit def sealedToBase(sealedBlueprint: SealedBlueprint): BaseBlueprint = BaseBlueprint(sealedBlueprint)

  implicit def sealedToDescriptor(sealedBlueprint: SealedBlueprint): BaseDescriptor = BaseDescriptor(sealedBlueprint)
}

case class BaseBlueprint(sealedBlueprint: SealedBlueprint) {

  val blueprintRef: BlueprintRef = sealedBlueprint.asMessage.sealedValue.value match {
    case blueprint: SourceBlueprint => blueprint.ref
    case blueprint: FilterBlueprint => blueprint.ref
    case blueprint: SinkBlueprint => blueprint.ref
    case blueprint: BranchBlueprint => blueprint.ref
    case blueprint: MergeBlueprint => blueprint.ref
  }
}


case class BaseDescriptor(sealedBlueprint: SealedBlueprint) {

  val descriptorRef: DescriptorRef = sealedBlueprint.asMessage.sealedValue.value match {
    case blueprint: SourceBlueprint => blueprint.descriptor
    case blueprint: FilterBlueprint => blueprint.descriptor
    case blueprint: SinkBlueprint => blueprint.descriptor
    case blueprint: BranchBlueprint => blueprint.descriptor
    case blueprint: MergeBlueprint => blueprint.descriptor
  }
}
