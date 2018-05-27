package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.Health.Health

object PipelineState {
  def apply(configuration: PipelineConfiguration, health: Health): PipelineState = new PipelineState(configuration.id, configuration, health)
}

case class PipelineState(id: UUID, configuration: PipelineConfiguration, health: Health)
