package io.logbee.keyscore.frontier.route.routes

import akka.actor.ActorRef
import akka.actor.FSM.Failure
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager.RequestExistingBlueprints
import io.logbee.keyscore.frontier.route.RouteImplicits
import io.logbee.keyscore.model.WhichValve.whichValve
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset

/**
  * The '''FilterRoute''' holds the REST route for all `Filters`.<br><br>
  * `Directives`: GET | PUT | POST | DELETE <br>
  * Operations: For a single Filter. <br>
  *
  * @todo Fix RequestExistingPipelines
  */
object FilterRoute extends RouteImplicits {

  def filterRoute(clusterPipelineManager: ActorRef): Route = {
    pathPrefix("filter") {
      pathPrefix(JavaUUID) { filterId =>
        path("pause") {
          post {
            parameter('value.as[Boolean]) { doPause =>
              onSuccess(clusterPipelineManager ? PauseFilter(filterId, doPause)) {
                case PauseFilterResponse(state) =>
                  complete(StatusCodes.Accepted, state)
                case Failure =>
                  complete(StatusCodes.InternalServerError)

              }
            }
          }
        } ~
          path("drain") {
            post {
              parameter('value.as[Boolean]) { doDrain =>
                onSuccess(clusterPipelineManager ? DrainFilterValve(filterId, doDrain)) {
                  case DrainFilterResponse(state) => complete(StatusCodes.Accepted, state)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
            }
          } ~
          path("insert") {
            put {
              entity(as[List[Dataset]]) { datasets =>
                parameter("where" ? "before") { where =>
                  onSuccess(clusterPipelineManager ? InsertDatasets(filterId, datasets, where)) {
                    case
                      InsertDatasetsResponse(state) => complete(StatusCodes.Accepted, state)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            }
          } ~
          path("extract") {
            get {
              parameters('value.as[Int], "where" ? "after") { (amount, where) =>
                onSuccess(clusterPipelineManager ? ExtractDatasets(filterId, amount, where)) {
                  case ExtractDatasetsResponse(datasets) => complete(StatusCodes.OK, datasets)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
            }
          } ~
          path("configurations") {
            put {
              entity(as[Configuration]) { configuration =>
                onSuccess(clusterPipelineManager ? ConfigureFilter(filterId, configuration)) {
                  case ConfigureFilterResponse(state) => complete(StatusCodes.Accepted, state)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
            } ~
              get {
                onSuccess(clusterPipelineManager ? RequestExistingBlueprints()) {
                  // TODO: Fix Me!
                  //                case PipelineConfigurationResponse(listOfConfigurations) => listOfConfigurations.flatMap(_.filter).find(_.id == filterId) match {
                  //                  case Some(filter) => complete(StatusCodes.OK, filter)
                  //                  case None => complete(StatusCodes.NotFound
                  //                  )
                  //                }
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
          } ~
          path("state") {
            get {
              onSuccess(clusterPipelineManager ? CheckFilterState(filterId)) {
                case CheckFilterStateResponse(state) =>
                  complete(StatusCodes.Accepted, state)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          path("clear") {
            get {
              onSuccess(clusterPipelineManager ? ClearBuffer(filterId)) {
                case ClearBufferResponse(state) =>
                  complete(StatusCodes.Accepted, state)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          path("scrape") {
            get {
              onSuccess(clusterPipelineManager ? ScrapeMetrics(filterId)) {
                case ScrapeMetricsResponse(metrics) =>
                  complete(StatusCodes.Accepted, metrics)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }
      }
    }
  }
}
