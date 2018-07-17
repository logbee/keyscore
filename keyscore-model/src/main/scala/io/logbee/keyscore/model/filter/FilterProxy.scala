package io.logbee.keyscore.model.filter
import java.util.UUID

import io.logbee.keyscore.model.Health

import scala.concurrent.Future

case class FilterState(id:UUID, health: Health, throughPutTime: Long = 0, totalThroughputTime: Long = 0, status: FilterStatus = Unknown)

trait FilterProxy {
  val id: UUID
  def configure(configuration: FilterConfiguration): Future[FilterState]
  def state(): Future[FilterState]
}
