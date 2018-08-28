package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration

case class PipelineConfiguration(id: UUID, name: String, description: String, source: Configuration, filter: List[Configuration], sink: Configuration)

