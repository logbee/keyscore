package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint}
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidFromString
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, SinkStage, SourceStage}

/**
  * The '''Pipeline''' is a wrapper for all objects and information needed to create a materialized Stream. <br>
  *
  * @todo The Source and the Sink are empty atm.
  * @todo Enhance this class for branching and merging.
  * @todo Implement a better logic for the `isComplete` function.
  *
  * @param pipelineBlueprint Blueprint for the Pipeline
  * @param sources A [[Map]] of the pipeline's sources.
  * @param sinks A [[Map]] of the pipeline's sinks.
  * @param filters A [[Map]] of the pipeline's filters.
  */
case class Pipeline(pipelineBlueprint: PipelineBlueprint, sources: Map[BlueprintRef, SourceStage] = Map.empty, sinks: Map[BlueprintRef, SinkStage] = Map.empty, filters: Map[BlueprintRef, FilterStage] = Map.empty) {

  val id: UUID = pipelineBlueprint.ref.uuid

  def withSourceStage(blueprintRef: BlueprintRef, newSource: SourceStage): Pipeline = {
    Pipeline(pipelineBlueprint, sources + (blueprintRef -> newSource), sinks, filters)
  }

  def withSinkStage(blueprintRef: BlueprintRef, newSink: SinkStage): Pipeline = {
    Pipeline(pipelineBlueprint, sources, sinks + (blueprintRef -> newSink), filters)
  }

  def withFilterStage(blueprintRef: BlueprintRef, newFilter: FilterStage): Pipeline = {
    Pipeline(pipelineBlueprint, sources, sinks, filters + (blueprintRef -> newFilter))
  }

  def isComplete: Boolean = {
    sources.nonEmpty && sinks.nonEmpty && (filters.size == pipelineBlueprint.blueprints.size -2)
  }

  def sourcesRefs: Set[BlueprintRef] = sources.keySet

  def filtersRefs: Set[BlueprintRef] = filters.keySet

  def sinksRefs: Set[BlueprintRef] = sinks.keySet
}
