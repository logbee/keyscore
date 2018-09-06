package io.logbee.keyscore.frontier.route.routes

import java.util.Locale

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.DescriptorRoute.{DescriptorRouteRequest, DescriptorRouteResponse}

object DescriptorRoute {

  case class DescriptorRouteRequest(clusterCapabilitiesManager: ActorRef)

  case class DescriptorRouteResponse(descriptorRoute: Route)

}

class DescriptorRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case DescriptorRouteRequest(clusterCapabilitiesManager) =>
      val r = descriptorsRoute(clusterCapabilitiesManager)
      sender ! DescriptorRouteResponse(r)
  }

  def descriptorsRoute(clusterCapabilitiesManager: ActorRef): Route = {
    pathPrefix("descriptors") {
      get {
        parameters('language.as[String]) { language =>
          onSuccess(clusterCapabilitiesManager ? GetStandardDescriptors(Locale.forLanguageTag(language))) {
            case StandardDescriptors(listOfDescriptors) => complete(StatusCodes.OK, listOfDescriptors)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }
}
