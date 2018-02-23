package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterModel}
import io.logbee.keyscore.model.sink.SinkModel
import io.logbee.keyscore.model.source.SourceModel

case class StreamModel(id: UUID, name: String, description: String, source: FilterConfiguration, sink: FilterConfiguration, filter: List[FilterConfiguration])
