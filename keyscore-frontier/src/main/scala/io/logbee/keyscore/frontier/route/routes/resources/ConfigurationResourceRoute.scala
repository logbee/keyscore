package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages._
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}

/**
  * The '''ConfigurationResourceRoute''' holds the REST route for all `Configuration` Resources.<br><br>
  * `Directives`: GET | PUT | POST | DELETE <br>
  * Operations: For all `Configurations` or a single one.
  */
object ConfigurationResourceRoute extends RouteImplicits {

  def configurationResourcesRoute(configurationManager: ActorRef) = {
    pathPrefix("resources") {
      pathPrefix("configuration") {
        pathPrefix("*") {
          get {
            onSuccess(configurationManager ? GetAllConfigurationRequest) {
              case GetAllConfigurationResponse(configurations) => complete(StatusCodes.OK, configurations.values.toSeq)
              case _ => complete(StatusCodes.InternalServerError)
            }
          } ~
            delete {
              onSuccess(configurationManager ? DeleteAllConfigurationsRequest) {
                case DeleteAllConfigurationsResponse => complete(StatusCodes.OK)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
        } ~
          pathPrefix(JavaUUID) { configurationId =>
            post {
              entity(as[Configuration]) { configuration =>
                onSuccess(configurationManager ? UpdateConfigurationRequest(configuration)) {
                  case UpdateConfigurationSuccessResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.NoContent)
                }
              }
            } ~
              put {
                entity(as[Configuration]) { configuration =>
                  onSuccess(configurationManager ? StoreOrUpdateConfigurationRequest(configuration)) {
                    case StoreConfigurationResponse => complete(StatusCodes.Created)
                    case UpdateConfigurationSuccessResponse => complete(StatusCodes.OK)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              } ~
              get {
                onSuccess((configurationManager ? GetConfigurationRequest(ConfigurationRef(configurationId.toString))).mapTo[GetConfigurationSuccess]) {
                  case GetConfigurationSuccess(configuration) => complete(StatusCodes.OK, configuration)
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
      }
    }
  }
}
