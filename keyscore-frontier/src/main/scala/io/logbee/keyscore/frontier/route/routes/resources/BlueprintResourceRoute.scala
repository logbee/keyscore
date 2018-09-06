package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.{Actor, ActorLogging}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.resources.BlueprintResourceRoute.{BlueprintResourceRouteRequest, BlueprintResourceRouteResponse}

object BlueprintResourceRoute {
  case object BlueprintResourceRouteRequest
  case object BlueprintResourceRouteResponse
}

class BlueprintResourceRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case BlueprintResourceRouteRequest =>
      sender ! BlueprintResourceRouteResponse
  }
}
