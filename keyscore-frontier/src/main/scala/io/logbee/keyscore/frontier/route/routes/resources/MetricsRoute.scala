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
        println(s"MetricsRoute: $id")
        put {
          println("MetricsRoute GET")
          entity(as[MetricsQuery]) { mq =>
            println(s"MetricsRoute: $mq")
            onSuccess(metricsManager ? RequestMetrics(id, mq)) {
              case MetricsResponseSuccess(_, metrics) =>
                println(s"MetricsRoute: ${metrics}")
                complete(StatusCodes.OK, metrics)
              case MetricsResponseFailure(_) =>
                println(s"MetricsRoute: Failure")
                complete(StatusCodes.NotFound, id)
              case _ =>
                println("MetricsRoute _")
                complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    }
  }



}
