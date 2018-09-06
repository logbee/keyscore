package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages._
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.resources.ConfigurationResourceRoute.{ConfigurationResourceRouteRequest, ConfigurationResourceRouteResponse}
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}

object ConfigurationResourceRoute {
  case class ConfigurationResourceRouteRequest(configurationManager: ActorRef)
  case class ConfigurationResourceRouteResponse(configurationRoute: Route)
}

//TODO Update this when the routes are tested
class ConfigurationResourceRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

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
          } ~
            get {
              onSuccess((configurationManager ? GetConfigurationRequest(ConfigurationRef(configurationId.toString))).mapTo[GetConfigurationResponse]) {
                case GetConfigurationResponse(configuration) => complete(StatusCodes.OK, configuration)
                case _ => complete(StatusCodes.InternalServerError)
              }
            } ~
            delete {
              onSuccess(configurationManager ? DeleteConfigurationRequest(ConfigurationRef(configurationId.toString))) {
                case DeleteConfigurationResponse => complete(StatusCodes.OK)
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
