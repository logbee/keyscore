package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.filter.FilterModel
import io.logbee.keyscore.model.sink.SinkModel
import io.logbee.keyscore.model.source.SourceModel

case class StreamModel(id: UUID, source: SourceModel, sink: SinkModel, filter: List[FilterModel])
