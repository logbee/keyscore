package io.logbee.keyscore.frontier.route.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetAllPipelineBlueprintsRequest, GetAllPipelineBlueprintsResponse}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.auth.AuthorizationHandler
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager.RequestExistingPipelines
import io.logbee.keyscore.frontier.cluster.pipeline.subordinates.PipelineDeployer.{BlueprintResolveFailure, NoAvailableAgents, PipelineDeployed}
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint}

/**
  * The '''PipelineRoute''' holds the REST route for all `Pipelines`.<br><br>
  * `Directives`: GET | PUT | POST | DELETE <br>
  * Operations: For a single Pipeline. | For a single or multiple Instances. <br>
  *
  * @todo Implement Route for Updating Pipelines
  */
trait PipelineRoute extends RouteImplicits with AuthorizationHandler {

  def pipelineRoute(clusterPipelineManager: ActorRef, blueprintManager: ActorRef): Route = {
    pathPrefix("pipeline") {
      authorize{ token =>
        pathPrefix("blueprint") {
          pathPrefix("*") {
            delete {
              clusterPipelineManager ! ClusterPipelineManager.DeleteAllPipelines
              complete(StatusCodes.OK)
            }
          } ~
            pathPrefix(JavaUUID) { configId =>
              get {
                onSuccess(blueprintManager ? GetAllPipelineBlueprintsRequest) {
                  case GetAllPipelineBlueprintsResponse(blueprints) =>
                    blueprints.find(blueprintEntry => blueprintEntry._1.uuid == configId.toString) match {
                      case Some(config) => complete(StatusCodes.OK, config)
                      case None => complete(StatusCodes.NotFound)
                    }
                  case _ => complete(StatusCodes.InternalServerError)
                }
              } ~
                delete {
                  clusterPipelineManager ! ClusterPipelineManager.DeletePipeline(id = configId)
                  complete(StatusCodes.OK)
                }
            } ~
            put {
              entity(as[BlueprintRef]) { blueprintRef =>
                onSuccess(clusterPipelineManager ? ClusterPipelineManager.CreatePipeline(blueprintRef)) {
                  case NoAvailableAgents => complete(StatusCodes.NoContent)
                  case BlueprintResolveFailure => complete(StatusCodes.Conflict)
                  case PipelineDeployed => complete(StatusCodes.Created)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
            } ~
            //TODO
            post {
              entity(as[PipelineBlueprint]) { blueprint =>
                complete(StatusCodes.NotImplemented)
              }
            }
        } ~
          pathPrefix("instance") {
            pathPrefix("*") {
              get {
                onSuccess(clusterPipelineManager ? RequestExistingPipelines) {
                  case PipelineInstanceResponse(listOfPipelines) => complete(StatusCodes.OK, listOfPipelines)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              } ~
                delete {
                  complete(StatusCodes.NotImplemented)
                }
            } ~
              pathPrefix(JavaUUID) { instanceId =>
                put {
                  complete(StatusCodes.NotImplemented)
                } ~
                  delete {
                    complete(StatusCodes.NotImplemented)
                  } ~
                  get {
                    onSuccess(clusterPipelineManager ? RequestExistingPipelines) {
                      case PipelineInstanceResponse(listOfPipelines) =>
                        listOfPipelines.find(instance => instance.id == instanceId) match {
                          case Some(instance) => complete(StatusCodes.OK, instance)
                          case None => complete(StatusCodes.NotFound)
                        }
                      case _ => complete(StatusCodes.InternalServerError)
                    }
                  }
              }
          }
        }
    }
  }
}
