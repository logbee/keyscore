package io.logbee.keyscore.model.notifications

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, OffsetDateTime, ZoneId}

import io.logbee.keyscore.model.data._

object NotificationConversion {

  def labelToField(label: Label): Field = {
    Field(label.name, label.value)
  }

  def notificationToRecord(notification: Notification): Record = {

    val idField = Field(NotificationAttributes.NOTIFICATION_ID, TextValue(notification.ref.toString))
    val timestamp = Field(NotificationAttributes.NOTIFICATION_TIMESTAMP, TimestampValue(notification.timestamp.get))
    val fields = notification.fields

    Record(List(idField, timestamp) ++ fields)
  }

  def convertNotificationCollectionToDataset(nc: NotificationsCollection): Dataset = {
    val records = nc.notifications.map { n =>
      notificationToRecord(n)
    }
    Dataset(records)
  }

  def getLatest(nc: NotificationsCollection): TimestampValue = {
    nc.notifications.map { notification => notification.timestamp.get }.reduceLeft(latest)
  }

  def getEarliest(nc: NotificationsCollection): TimestampValue = {
    nc.notifications.map { notification => notification.timestamp.get }.reduce(earliest)
  }

  private def latest(first: TimestampValue, second: TimestampValue): TimestampValue = {
    if (first.seconds < second.seconds) second
    else if (first.seconds == second.seconds) {
      if (first.nanos < second.nanos) second
      else first
    }
    else first
  }

  private def earliest(first: TimestampValue, second: TimestampValue): TimestampValue = {
    if (first.seconds > second.seconds) second
    else if (first.seconds == second.seconds) {
      if (first.nanos > second.seconds) second
      else first
    }
    else first
  }

  def timestampToString(tsv: TimestampValue, format: String): String = {
    val localDateTime = LocalDateTime.ofEpochSecond(tsv.seconds, tsv.nanos, OffsetDateTime.now(ZoneId.systemDefault()).getOffset)

    localDateTime.format(DateTimeFormatter.ofPattern(format))
  }

}
