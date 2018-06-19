package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.filter.FilterConfiguration

case class PipelineConfiguration(id: UUID, name: String, description: String, source: FilterConfiguration, filter: List[FilterConfiguration], sink: FilterConfiguration)

