package io.logbee.keyscore.model.source

import java.util.UUID

import io.logbee.keyscore.model.filter.FilterConfiguration

import scala.concurrent.Future

trait SourceProxy {
  val id: UUID
  def configure(configuration: FilterConfiguration): Future[Unit]
}
