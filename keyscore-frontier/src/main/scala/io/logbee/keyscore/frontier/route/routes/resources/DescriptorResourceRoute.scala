package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{JavaUUID, as, complete, delete, entity, get, onSuccess, pathPrefix, post, put, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages._
import io.logbee.keyscore.frontier.auth.AuthorizationHandler
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

/**
  * The '''DescriptorResourceRoute''' holds the REST route for all `Descriptor` Resources.<br><br>
  * `Directives`: GET | PUT | POST | DELETE <br>
  * Operations: For all `Descriptors` or a single one.
  */
trait DescriptorResourceRoute extends RouteImplicits with AuthorizationHandler {

  def descriptorResourcesRoute(descriptorManager: ActorRef): Route = {
    pathPrefix("resources") {
      authorize { token =>
        pathPrefix("descriptor") {
          pathPrefix("*") {
            get {
              onSuccess(descriptorManager ? GetAllDescriptorsRequest) {
                case GetAllDescriptorsResponse(descriptors) => complete(StatusCodes.OK, descriptors)
                case _ => complete(StatusCodes.InternalServerError)
              }
            } ~
              delete {
                onSuccess(descriptorManager ? DeleteAllDescriptorsRequest) {
                  case DeleteAllDescriptorsResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
          } ~
            pathPrefix(JavaUUID) { descriptorId =>
              post {
                entity(as[Descriptor]) { descriptor =>
                  onSuccess(descriptorManager ? UpdateDescriptorRequest(descriptor)) {
                    case UpdateDescriptorSuccessResponse => complete(StatusCodes.OK)
                    case _ => complete(StatusCodes.NoContent)
                  }
                }
              } ~
                put {
                  entity(as[Descriptor]) { descriptor =>
                    onSuccess(descriptorManager ? StoreDescriptorRequest(descriptor)) {
                      case StoreDescriptorResponse => complete(StatusCodes.Created)
                      case _ => complete(StatusCodes.InternalServerError)
                    }
                  }
                } ~
                get {
                  onSuccess((descriptorManager ? GetDescriptorRequest(DescriptorRef(descriptorId.toString))).mapTo[GetDescriptorResponse]) {
                    case GetDescriptorResponse(descriptor) => complete(StatusCodes.OK, descriptor)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                } ~
                delete {
                  onSuccess(descriptorManager ? DeleteDescriptorRequest(DescriptorRef(descriptorId.toString))) {
                    case DeleteDescriptorResponse => complete(StatusCodes.OK)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
            }
        }
      }
    }
  }
}
