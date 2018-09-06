package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages._
import io.logbee.keyscore.frontier.route.routes.resources.ConfigurationResourceRoute.{ConfigurationResourceRouteRequest, ConfigurationResourceRouteResponse}
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization

import scala.concurrent.duration._

object ConfigurationResourceRoute {
  case class ConfigurationResourceRouteRequest(configurationManager: ActorRef)
  case class ConfigurationResourceRouteResponse(configurationRoute: Route)
}

class ConfigurationResourceRoute extends Actor with ActorLogging with Json4sSupport{

  implicit val timeout: Timeout = 30 seconds
  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  override def receive: Receive = {
    case ConfigurationResourceRouteRequest(configurationManager) =>
      val r = configurationResourcesRoute(configurationManager)
      sender ! ConfigurationResourceRouteResponse(r)
  }

  def configurationResourcesRoute(configurationManager: ActorRef) = {
    pathPrefix("resources") {
      pathPrefix("configuration") {
        pathPrefix(JavaUUID) { configurationId =>
          put {
            entity(as[Configuration]) { configuration =>
              onSuccess(configurationManager ? StoreConfigurationRequest(configuration)) {
                case StoreConfigurationResponse => complete(StatusCodes.Created)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }~
            get {
              onSuccess((configurationManager ? GetConfigurationRequest(ConfigurationRef(configurationId.toString))).mapTo[GetConfigurationResponse]) {
                case GetConfigurationResponse(configuration) => complete(StatusCodes.OK, configuration)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
        }
      } ~
        get {
          onSuccess(configurationManager ? GetAllConfigurationRequest) {
            case GetAllConfigurationResponse(configurations) => complete(StatusCodes.OK, configurations)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
    }
  }
}
