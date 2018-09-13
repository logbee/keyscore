package io.logbee.keyscore.frontier.route.routes

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentCapabilitiesManager.{GetDescriptors, GetDescriptorsResponse}
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.DescriptorRoute.{DescriptorRouteRequest, DescriptorRouteResponse}

object DescriptorRoute {

  case class DescriptorRouteRequest(agentCapabilitiesManager: ActorRef)

  case class DescriptorRouteResponse(descriptorRoute: Route)

}

class DescriptorRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case DescriptorRouteRequest(agentCapabilitiesManager) =>
      val r = descriptorsRoute(agentCapabilitiesManager)
      sender ! DescriptorRouteResponse(r)
  }

  def descriptorsRoute(agentCapabilitiesManager: ActorRef): Route = {
    pathPrefix("descriptors") {
      get {
        onSuccess(agentCapabilitiesManager ? GetDescriptors) {
          case GetDescriptorsResponse(listOfDescriptors) => complete(StatusCodes.OK, listOfDescriptors)
          case _ => complete(StatusCodes.InternalServerError)
        }
      }
    }
  }
}
