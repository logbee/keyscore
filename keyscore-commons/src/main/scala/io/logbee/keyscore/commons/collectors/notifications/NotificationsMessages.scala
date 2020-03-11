package io.logbee.keyscore.commons.collectors.notifications

import java.util.UUID

import akka.actor.ActorRef
import io.logbee.keyscore.commons.notifications.NotificationsQuery
import io.logbee.keyscore.model.notifications.NotificationsCollection

case class RequestNotifications(id: UUID, nq: NotificationsQuery)
case class NotificationsResponseSuccess(id: UUID, Notifications: Seq[NotificationsCollection])
case class NotificationsResponseFailure(id: UUID)

case class ScrapeNotifications(ref: ActorRef)