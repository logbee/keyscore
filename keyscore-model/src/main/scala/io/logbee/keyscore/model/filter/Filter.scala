package io.logbee.keyscore.model.filter

import io.logbee.keyscore.model.Condition

import scala.concurrent.Future

trait Filter {
  def configure(configuration: FilterConfiguration): Future[Unit]
}
