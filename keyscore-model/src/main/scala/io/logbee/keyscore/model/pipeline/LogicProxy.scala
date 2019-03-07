package io.logbee.keyscore.model.pipeline

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Label
import io.logbee.keyscore.model.metrics.MetricsCollection

import scala.concurrent.Future

trait LogicProxy {
  val id: UUID
  def configure(configuration: Configuration): Future[FilterState]
  def state(): Future[FilterState]
  def scrape(labels: Set[Label] = Set.empty): Future[MetricsCollection]
}
