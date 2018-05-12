package io.logbee.keyscore.model.source

import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

trait Source {
  def configure(configuration: FilterConfiguration): Future[Unit]
}
