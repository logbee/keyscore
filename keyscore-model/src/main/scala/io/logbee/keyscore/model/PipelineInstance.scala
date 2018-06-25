package io.logbee.keyscore.model

import java.util.UUID
import java.util.UUID.fromString

object PipelineInstance {
  def apply(configurationId: UUID, name: String, description: String, health: Health): PipelineInstance = new PipelineInstance(configurationId, name, description, configurationId, health)

  def apply(health: Health) = new PipelineInstance(fromString("00000000-0000-0000-0000-000000000000"), "", "", null, health)
}

case class PipelineInstance(id: UUID, name: String, description: String, configurationId: UUID, health: Health)
