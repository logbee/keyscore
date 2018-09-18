package io.logbee.keyscore.model.blueprint

import io.logbee.keyscore.model.configuration.ConfigurationRef
import io.logbee.keyscore.model.descriptor.DescriptorRef


object BlueprintWrapper {
  def wrap(blueprint: SealedBlueprint): BlueprintWrapper[_] = blueprint match {
    case blueprint: FilterBlueprint => new FilterBlueprintWrapper(blueprint)
    case blueprint: SourceBlueprint => new SourceBlueprintWrapper(blueprint)
    case blueprint: SinkBlueprint => new SinkBlueprintWrapper(blueprint)
    case blueprint: BranchBlueprint => new BranchBlueprintWrapper(blueprint)
    case blueprint: MergeBlueprint => new MergeBlueprintWrapper(blueprint)
    case _ => throw new IllegalArgumentException("Blueprint has to be one of FilterBlueprint, SourceBlueprint, SinkBlueprint, BranchBlueprint or MergeBlueprint!")
  }
}

sealed trait BlueprintWrapper[T <: SealedBlueprint] {
  val blueprint: T
  def blueprintRef: BlueprintRef
  def descriptorRef: DescriptorRef
  def configurationRef: ConfigurationRef
}

class FilterBlueprintWrapper(override val blueprint: FilterBlueprint) extends BlueprintWrapper[FilterBlueprint] {
  override def blueprintRef: BlueprintRef = blueprint.ref
  override def descriptorRef: DescriptorRef = blueprint.descriptor
  override def configurationRef: ConfigurationRef = blueprint.configuration
}

class SourceBlueprintWrapper(override val blueprint: SourceBlueprint) extends BlueprintWrapper[SourceBlueprint] {
  override def blueprintRef: BlueprintRef = blueprint.ref
  override def descriptorRef: DescriptorRef = blueprint.descriptor
  override def configurationRef: ConfigurationRef = blueprint.configuration
}

class SinkBlueprintWrapper(override val blueprint: SinkBlueprint) extends BlueprintWrapper[SinkBlueprint] {
  override def blueprintRef: BlueprintRef = blueprint.ref
  override def descriptorRef: DescriptorRef = blueprint.descriptor
  override def configurationRef: ConfigurationRef = blueprint.configuration
}

class BranchBlueprintWrapper(override val blueprint: BranchBlueprint) extends BlueprintWrapper[BranchBlueprint] {
  override def blueprintRef: BlueprintRef = blueprint.ref
  override def descriptorRef: DescriptorRef = blueprint.descriptor
  override def configurationRef: ConfigurationRef = blueprint.configuration
}

class MergeBlueprintWrapper(override val blueprint: MergeBlueprint) extends BlueprintWrapper[MergeBlueprint] {
  override def blueprintRef: BlueprintRef = blueprint.ref
  override def descriptorRef: DescriptorRef = blueprint.descriptor
  override def configurationRef: ConfigurationRef = blueprint.configuration
}
