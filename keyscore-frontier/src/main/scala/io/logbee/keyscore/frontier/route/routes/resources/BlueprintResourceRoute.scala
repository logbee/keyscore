package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages._
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.resources.BlueprintResourceRoute.{BlueprintResourceRouteRequest, BlueprintResourceRouteResponse}
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}

object BlueprintResourceRoute {
  case class BlueprintResourceRouteRequest(blueprintManager: ActorRef)
  case class BlueprintResourceRouteResponse(blueprintRoute: Route)
}

//TODO Update this when the routes are tested
class BlueprintResourceRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case BlueprintResourceRouteRequest(blueprintManager) =>
      val r = blueprintResourceRoute(blueprintManager)
      sender ! BlueprintResourceRouteResponse(r)
  }
  
  def blueprintResourceRoute(blueprintManager: ActorRef): Route = {
    pathPrefix("resources") {
      pathPrefix("blueprint") {
        pathPrefix("pipeline") {
          pathPrefix(JavaUUID) { pipelineBlueprintId =>
            post {
              entity(as[PipelineBlueprint]) { pipelineBlueprint =>
                onSuccess(blueprintManager ? UpdatePipelineBlueprintRequest(pipelineBlueprint)) {
                  case UpdatePipelineBlueprintResponseSuccess => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.NoContent)
                }
              }
            } ~
              put {
                entity(as[PipelineBlueprint]) { pipelineBlueprint =>
                  onSuccess(blueprintManager ? StorePipelineBlueprintRequest(pipelineBlueprint)) {
                    case StorePipelineBlueprintResponse => complete(StatusCodes.Created)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              } ~
              get {
                onSuccess((blueprintManager ? GetPipelineBlueprintRequest(BlueprintRef(pipelineBlueprintId.toString))).mapTo[GetPipelineBlueprintResponse]) {
                  case GetPipelineBlueprintResponse(blueprint) => complete(StatusCodes.OK, blueprint)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              } ~
              delete {
                onSuccess(blueprintManager ? DeletePipelineBlueprintRequest(BlueprintRef(pipelineBlueprintId.toString))) {
                  case DeletePipelineBlueprintResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
          } ~
          pathPrefix("*") {
            get {
              onSuccess(blueprintManager ? GetAllPipelineBlueprintsRequest) {
                case GetAllPipelineBlueprintsResponse(pipelineBlueprints) => complete(StatusCodes.OK, pipelineBlueprints)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
        pathPrefix(JavaUUID) { blueprintId =>
            post {
              entity(as[SealedBlueprint]) { blueprint =>
                onSuccess(blueprintManager ? UpdateBlueprintRequest(blueprint)) {
                  case UpdateBlueprintResponseSuccess => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.NoContent)
                }
              }
            } ~
              put {
                entity(as[SealedBlueprint]) { blueprint =>
                  onSuccess(blueprintManager ? StoreBlueprintRequest(blueprint)) {
                    case StoreBlueprintResponse => complete(StatusCodes.Created)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              } ~
              get {
                onSuccess((blueprintManager ? GetBlueprintRequest(BlueprintRef(blueprintId.toString))).mapTo[GetBlueprintResponse]) {
                  case GetBlueprintResponse(blueprint) => complete(StatusCodes.OK, blueprint)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              } ~
              delete {
                onSuccess(blueprintManager ? DeleteBlueprintRequest(BlueprintRef(blueprintId.toString))) {
                  case DeleteBlueprintResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
          } ~
          pathPrefix("*") {
            get {
              onSuccess(blueprintManager ? GetAllBlueprintsRequest) {
                case GetAllBlueprintsResponse(blueprints) => complete(StatusCodes.OK, blueprints)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }
      } 
    }
  }
}
