package io.logbee.keyscore.model.sink

import java.util.UUID

import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

trait SinkProxy {
  val id: UUID
  def configure(configuration: FilterConfiguration): Future[Unit]
}
