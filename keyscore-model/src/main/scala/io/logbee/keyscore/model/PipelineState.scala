package io.logbee.keyscore.model

import java.util.UUID
import java.util.UUID.fromString

import io.logbee.keyscore.model.Health.Health

object PipelineState {
  def apply(configuration: PipelineConfiguration, health: Health): PipelineState = new PipelineState(configuration.id, configuration, health)
  def apply(health: Health) = new PipelineState(fromString("00000000-0000-0000-0000-000000000000"), null, health)
}

case class PipelineState(id: UUID, configuration: PipelineConfiguration, health: Health)
