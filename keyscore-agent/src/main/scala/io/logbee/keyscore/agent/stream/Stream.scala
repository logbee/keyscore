package io.logbee.keyscore.agent.stream

import java.util.UUID

import io.logbee.keyscore.agent.stream.stage.{FilterStage, SinkStage, SourceStage}
import io.logbee.keyscore.model.StreamConfiguration


case class Stream(configuration: StreamConfiguration, source: Option[SourceStage] = None, sink: Option[SinkStage] = None, filters: List[FilterStage] = List.empty) {

  val id: UUID = configuration.id

  def withSource(newSource: SourceStage): Stream = {
    Stream(configuration, Option(newSource), sink, filters)
  }

  def withSink(newSink: SinkStage): Stream = {
    Stream(configuration, source, Option(newSink), filters)
  }

  def withFilter(newFilter: FilterStage): Stream = {
    Stream(configuration, source, sink, filters :+ newFilter)
  }

  def isComplete: Boolean = {
    source.isDefined && sink.isDefined
  }
}
