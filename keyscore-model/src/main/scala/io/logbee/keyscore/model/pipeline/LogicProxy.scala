package io.logbee.keyscore.model.pipeline

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Label
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.model.notifications.NotificationsCollection

import scala.concurrent.Future

trait LogicProxy {
  val id: UUID
  def configure(configuration: Configuration): Future[FilterState]
  def state(): Future[FilterState]
  def scrapeMetrics(labels: Set[Label] = Set.empty): Future[MetricsCollection]
  def scrapeNotifications: Future[NotificationsCollection]
}
