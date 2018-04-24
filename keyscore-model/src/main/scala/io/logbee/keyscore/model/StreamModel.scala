package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.filter.FilterConfiguration

case class StreamModel(id: UUID, name: String, description: String, source: FilterConfiguration, sink: FilterConfiguration, filter: List[FilterConfiguration])
