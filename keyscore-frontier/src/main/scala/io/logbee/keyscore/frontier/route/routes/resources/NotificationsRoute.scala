package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{JavaUUID, as, complete, entity, onSuccess, pathPrefix, post}
import akka.http.scaladsl.server.Route
import io.logbee.keyscore.commons.collectors.notifications.{NotificationsResponseFailure, NotificationsResponseSuccess, RequestNotifications}
import io.logbee.keyscore.commons.notifications.NotificationsQuery
import io.logbee.keyscore.frontier.auth.AuthorizationHandler
import io.logbee.keyscore.frontier.route.RouteImplicits
import akka.pattern.ask

trait NotificationsRoute extends RouteImplicits with AuthorizationHandler {

  def notificationsRoute(notificationsManager: ActorRef): Route = {
    pathPrefix("notifications") {
      pathPrefix(JavaUUID) { id =>
        post {
          entity(as[NotificationsQuery]) { mq =>
            onSuccess(notificationsManager ? RequestNotifications(id, mq)) {
              case NotificationsResponseSuccess(_, notifications) =>
                complete(StatusCodes.OK, notifications)
              case NotificationsResponseFailure(_) =>
                complete(StatusCodes.NotFound, id)
              case e =>
                println(s"NotificationsQuery went wrong: $e")
                complete(StatusCodes.InternalServerError, e)
            }
          }
        }
      }
    }
  }

}
