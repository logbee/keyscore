package io.logbee.keyscore.pipeline.api.collectors.notifications

import io.logbee.keyscore.model.notifications.{Notification, NotificationRef, NotificationsCollection}

import scala.collection.mutable

class NotificationsCollector {

  private val notifications: mutable.Map[String, Seq[Notification]] = mutable.HashMap.empty[String, Seq[Notification]]

  def collect(ref: NotificationRef, notification: Notification): NotificationsCollector = new NotificationsCollector {
    notifications.update(ref.uuid, createOrUpdate(ref.uuid).appended(notification))
  }

  def scrape(): NotificationsCollection = {
    val collection = NotificationsCollection(notifications.values.flatten.toList)
    notifications.clear()
    collection
  }

  private def createOrUpdate(uuid: String): Seq[Notification] = {
    notifications.getOrElse(uuid, Seq())
  }

}
