package io.logbee.keyscore.model.blueprint

object ToBaseBlueprint {
  implicit def sealedToBase(sealedBlueprint: SealedBlueprint): BaseBlueprint = BaseBlueprint(sealedBlueprint)
}

case class BaseBlueprint(sealedBlueprint: SealedBlueprint) {

  val ref: BlueprintRef = sealedBlueprint.asMessage.sealedValue.value match {
    case blueprint: SourceBlueprint => blueprint.ref
    case blueprint: FilterBlueprint => blueprint.ref
    case blueprint: SinkBlueprint => blueprint.ref
    case blueprint: BranchBlueprint => blueprint.ref
    case blueprint: MergeBlueprint => blueprint.ref
  }
}
