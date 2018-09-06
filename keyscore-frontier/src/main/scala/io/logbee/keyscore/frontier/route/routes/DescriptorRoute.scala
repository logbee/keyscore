package io.logbee.keyscore.frontier.route.routes

import java.util.Locale

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.route.routes.DescriptorRoute.{DescriptorRouteRequest, DescriptorRouteResponse}
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization

import scala.concurrent.duration._

object DescriptorRoute {
  case class DescriptorRouteRequest(clusterCapabilitiesManager: ActorRef)
  case class DescriptorRouteResponse(descriptorRoute: Route)

}

class DescriptorRoute extends Actor with ActorLogging with Json4sSupport {

  implicit val timeout: Timeout = 30 seconds
  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

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
