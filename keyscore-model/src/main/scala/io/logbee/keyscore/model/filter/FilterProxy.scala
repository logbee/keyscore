package io.logbee.keyscore.model.filter

import java.util.UUID

import io.logbee.keyscore.model.Health.Health

import scala.concurrent.Future

case class FilterState(id:UUID, health: Health)

trait FilterProxy {
  val id: UUID
  def configure(configuration: FilterConfiguration): Future[Unit]
  def state(): Future[FilterState]
}
