package io.logbee.keyscore.model.filter

import scala.concurrent.Future

trait FilterProxy {
  def configure(configuration: FilterConfiguration): Future[Unit]
}
