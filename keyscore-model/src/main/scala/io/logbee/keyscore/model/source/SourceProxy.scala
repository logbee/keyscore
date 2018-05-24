package io.logbee.keyscore.model.source

import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

trait SourceProxy {
  def configure(configuration: FilterConfiguration): Future[Unit]
}
