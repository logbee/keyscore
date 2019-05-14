package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import io.logbee.keyscore.commons.metrics.{MetricsQuery, MetricsResponseFailure, MetricsResponseSuccess, RequestMetrics}
import io.logbee.keyscore.frontier.auth.AuthorizationHandler
import io.logbee.keyscore.frontier.route.RouteImplicits

trait MetricsRoute extends RouteImplicits with AuthorizationHandler {

  def metricsRoute(metricsManager: ActorRef): Route = {
    pathPrefix("metrics") {
      pathPrefix(JavaUUID) { id =>
        get {
          entity(as[MetricsQuery]) { mq =>
            onSuccess(metricsManager ? RequestMetrics(id, mq)) {
              case MetricsResponseSuccess(_, metrics) => complete(StatusCodes.OK, metrics)
              case MetricsResponseFailure(_) => complete(StatusCodes.NotFound, id)
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  }



}
