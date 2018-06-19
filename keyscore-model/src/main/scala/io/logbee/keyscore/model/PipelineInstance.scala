package io.logbee.keyscore.model

import java.util.UUID
import java.util.UUID.fromString

import io.logbee.keyscore.model.Health.Health

object PipelineInstance {
  def apply(configuration: PipelineConfiguration, health: Health): PipelineInstance = new PipelineInstance(configuration.id, configuration, health)
  def apply(health: Health) = new PipelineInstance(fromString("00000000-0000-0000-0000-000000000000"), null, health)
}

case class PipelineInstance(id: UUID, configuration: PipelineConfiguration, health: Health)
