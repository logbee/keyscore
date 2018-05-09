package io.logbee.keyscore.model.sink

import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

trait Sink {
  def configure(configuration: FilterConfiguration): Future[Unit]
}
