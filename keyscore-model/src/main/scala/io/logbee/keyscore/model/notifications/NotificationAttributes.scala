package io.logbee.keyscore.model.notifications

sealed trait NotificationType

object NotificationAttributes {
  /**
    * Specifies the ID (eg. filter or agent)
    */
  val NOTIFICATION_ID: String = "notification.id"
  val NOTIFICATION_TIMESTAMP: String = "notification.timestamp"
  val NOTIFICATION_FIELDS: String = "notification.fields"

}