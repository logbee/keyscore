package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, SinkStage, SourceStage}
import io.logbee.keyscore.model.PipelineConfiguration


case class Pipeline(configuration: PipelineConfiguration, source: Option[SourceStage] = None, sink: Option[SinkStage] = None, filters: List[FilterStage] = List.empty) {

  val id: UUID = configuration.id

  def withSourceStage(newSource: SourceStage): Pipeline = {
    Pipeline(configuration, Option(newSource), sink, filters)
  }

  def withSinkStage(newSink: SinkStage): Pipeline = {
    Pipeline(configuration, source, Option(newSink), filters)
  }

  def withFilterStage(newFilter: FilterStage): Pipeline = {
    Pipeline(configuration, source, sink, filters :+ newFilter)
  }

  def isComplete: Boolean = {
    source.isDefined && sink.isDefined
  }
}
