package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidFromString
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, SinkStage, SourceStage}


case class Pipeline(pipelineBlueprint: PipelineBlueprint, source: Option[SourceStage] = None, sink: Option[SinkStage] = None, filters: List[FilterStage] = List.empty) {

  val id: UUID = pipelineBlueprint.ref.uuid

  def withSourceStage(newSource: SourceStage): Pipeline = {
    Pipeline(pipelineBlueprint, Option(newSource), sink, filters)
  }

  def withSinkStage(newSink: SinkStage): Pipeline = {
    Pipeline(pipelineBlueprint, source, Option(newSink), filters)
  }

  def withFilterStage(newFilter: FilterStage): Pipeline = {
    Pipeline(pipelineBlueprint, source, sink, filters :+ newFilter)
  }

  def isComplete: Boolean = {
    source.isDefined && sink.isDefined
  }
}
