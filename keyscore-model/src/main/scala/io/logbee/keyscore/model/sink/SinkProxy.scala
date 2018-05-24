package io.logbee.keyscore.model.sink

import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

trait SinkProxy {
  def configure(configuration: FilterConfiguration): Future[Unit]
}
