package io.logbee.keyscore.frontier.route.routes.resources

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages._
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}

/**
  * The '''BlueprintResourceRoute''' holds the REST route for all `Blueprint` Resources.<br><br>
  * `Directives`: GET | PUT | POST | DELETE <br>
  * Operations: For all `Blueprints` or a single one. <br>
  * Differentiation: `PipelineBlueprint` | `SealedBlueprint`
  */
object BlueprintResourceRoute extends RouteImplicits {

  def blueprintResourceRoute(blueprintManager: ActorRef): Route = {
    pathPrefix("resources") {
      pathPrefix("blueprint") {
        pathPrefix("pipeline") {
          pathPrefix("*") {
            get {
              onSuccess(blueprintManager ? GetAllPipelineBlueprintsRequest) {
                case GetAllPipelineBlueprintsResponse(pipelineBlueprints) => complete(StatusCodes.OK, pipelineBlueprints)
                case _ => complete(StatusCodes.InternalServerError)
              }
            } ~
              delete {
                onSuccess(blueprintManager ? DeleteAllPipelineBlueprintsRequest) {
                  case DeleteAllPipelineBlueprintsResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
          } ~
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
                case GetAllBlueprintsResponse(blueprints) => complete(StatusCodes.OK, blueprints.values.toSeq)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }
      }
    }
  }
}
