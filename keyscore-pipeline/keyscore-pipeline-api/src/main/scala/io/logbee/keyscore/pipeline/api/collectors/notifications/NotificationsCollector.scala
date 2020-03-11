package io.logbee.keyscore.pipeline.api.collectors.notifications

import java.util.UUID

import com.google.protobuf.util.Timestamps
import io.logbee.keyscore.model.data.{Field, TimestampValue}
import io.logbee.keyscore.model.notifications.{Notification, NotificationRef, NotificationsCollection}
import org.joda.time.chrono.AssembledChronology.Fields

import scala.collection.mutable

class NotificationsCollector(uuid: String) {

  /**
    * k: ID of the Block / Filter
    * v: Seq(Notifications)
    */
  private val notifications: scala.collection.mutable.ListBuffer[Notification] = mutable.ListBuffer()

  /**
    * Standard Fields: <br>
    * * `level`: NotificationValue | e.g. "debug", "error". <br>
    * * `message`: TextValue | e.g. the message provided by the exception. <br>
    * * `dataset`: TextValue | e.g. the dataset that was processed.
    * @param fields
    */
  def notifyy(fields: Seq[Field]): Unit =  {
    notifications += Notification(
      NotificationRef(uuid = uuid),
      Some(now),
      fields
    )
  }

  def scrape(): NotificationsCollection = {
    val collection = NotificationsCollection(notifications.toList)
    collection
  }

  private def now: TimestampValue = {
    val now = Timestamps.fromMillis(System.currentTimeMillis())
    TimestampValue(now.getSeconds, now.getNanos)
  }

}
