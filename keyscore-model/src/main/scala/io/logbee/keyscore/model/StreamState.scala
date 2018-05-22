package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.Health.Health

object StreamState {
  def apply(configuration: StreamConfiguration, health: Health): StreamState = new StreamState(configuration.id, configuration, health)
}

case class StreamState(id: UUID, configuration: StreamConfiguration, health: Health)
