package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{JavaUUID, as, complete, delete, entity, get, onSuccess, pathPrefix, post, put, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages._
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.resources.DescriptorResourceRoute.{DescriptorResourceRouteRequest, DescriptorResourceRouteResponse}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

object DescriptorResourceRoute {

  case class DescriptorResourceRouteRequest(descriptorManager: ActorRef)

  case class DescriptorResourceRouteResponse(descriptorResoures: Route)

}

/**
  * The '''DescriptorResourceRoute''' holds the REST route for all `Descriptor` Resources.<br><br>
  * `Directives`: GET | PUT | POST | DELETE <br>
  * Operations: For all `Descriptors` or a single one.
  */
class DescriptorResourceRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case DescriptorResourceRouteRequest(descriptorManager) =>
      val r = descriptorResourcesRoute(descriptorManager)
      sender ! DescriptorResourceRouteResponse(r)
  }

  def descriptorResourcesRoute(descriptorManager: ActorRef): Route = {
    pathPrefix("resources") {
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
