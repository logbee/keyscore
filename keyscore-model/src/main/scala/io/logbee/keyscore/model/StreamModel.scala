package io.logbee.keyscore.model

import io.logbee.keyscore.model.filter.FilterModel
import io.logbee.keyscore.model.sink.SinkModel
import io.logbee.keyscore.model.source.SourceModel

case class StreamModel(id: String, source: SourceModel, sink: SinkModel, filter: List[FilterModel])
