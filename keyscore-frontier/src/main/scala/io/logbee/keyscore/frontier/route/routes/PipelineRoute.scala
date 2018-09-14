package io.logbee.keyscore.frontier.route.routes

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetAllPipelineBlueprintsRequest, GetAllPipelineBlueprintsResponse}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager.{RequestExistingBlueprints, RequestExistingPipelines}
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.frontier.route.routes.PipelineRoute.{PipelineRouteRequest, PipelineRouteResponse}
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint}

object PipelineRoute {
  case class PipelineRouteRequest(clusterPipelineManager: ActorRef, blueprintManager: ActorRef)
  case class PipelineRouteResponse(pipelineRoute: Route)
}

class PipelineRoute extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  override def receive: Receive = {
    case PipelineRouteRequest(pipelineManager, blueprintManager) =>
      val r = pipelineRoute(pipelineManager, blueprintManager)
      sender ! PipelineRouteResponse(r)
  }

  def pipelineRoute(pipelineManager: ActorRef, blueprintManager: ActorRef): Route = {
    pathPrefix("pipeline") {
      pathPrefix("configuration") {
        pathPrefix("*") {
          get {
            onSuccess(pipelineManager ? RequestExistingBlueprints()) {
              case PipelineBlueprintsResponse(listOfConfigurations) => complete(StatusCodes.OK, listOfConfigurations)
              case _ => complete(StatusCodes.InternalServerError)
            }
          } ~
            delete {
              pipelineManager ! ClusterPipelineManager.DeleteAllPipelines
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
                pipelineManager ! ClusterPipelineManager.DeletePipeline(id = configId)
                complete(StatusCodes.OK)
              }
          } ~
          put {
            entity(as[BlueprintRef]) { blueprintRef =>
              pipelineManager ! ClusterPipelineManager.CreatePipeline(blueprintRef)
              complete(StatusCodes.Created)
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
              onSuccess(pipelineManager ? RequestExistingPipelines) {
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
                  onSuccess(pipelineManager ? RequestExistingPipelines) {
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
