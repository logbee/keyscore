package io.logbee.keyscore.model.sink

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.filter.FilterState

import scala.concurrent.Future

trait SinkProxy {
  val id: UUID
  def configure(configuration: Configuration): Future[FilterState]
}
