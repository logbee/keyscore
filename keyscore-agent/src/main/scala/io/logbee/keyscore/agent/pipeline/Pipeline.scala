package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint}
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidFromString
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, SinkStage, SourceStage}

import scala.collection.mutable._

/**
  * The '''Pipeline''' is a wrapper for all objects and information needed to create a materialized Stream. <br>
  *
  * @todo The Source and the Sink are empty atm.
  * @todo Enhance this class for branching and merging.
  * @todo Implement a better logic for the `isComplete` function.
  *
  * @param pipelineBlueprint Blueprint for the Pipeline
  * @param source The first element of the materialized Stream
  * @param sink The last element of the materialized Stream
  * @param filters The elements between the Source and the Sink
  */
case class Pipeline(pipelineBlueprint: PipelineBlueprint, source: Option[SourceStage] = None, sink: Option[SinkStage] = None, filters: HashMap[BlueprintRef, FilterStage] = HashMap.empty) {

  val id: UUID = pipelineBlueprint.ref.uuid

  def withSourceStage(blueprintRef: BlueprintRef, newSource: SourceStage): Pipeline = {
    Pipeline(pipelineBlueprint, Option(newSource), sink, filters)
  }

  def withSinkStage(blueprintRef: BlueprintRef, newSink: SinkStage): Pipeline = {
    Pipeline(pipelineBlueprint, source, Option(newSink), filters)
  }

  def withFilterStage(blueprintRef: BlueprintRef, newFilter: FilterStage): Pipeline = {
    Pipeline(pipelineBlueprint, source, sink, filters += (blueprintRef -> newFilter))
  }

  def isComplete: Boolean = {
    source.isDefined && sink.isDefined && (filters.size == pipelineBlueprint.blueprints.size -2)
  }
}
