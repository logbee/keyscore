package io.logbee.keyscore.frontier.route.routes

import akka.actor.FSM.Failure
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.cluster.PipelineManager.RequestExistingBlueprints
import io.logbee.keyscore.frontier.route.routes.FilterRoute.{FilterRouteRequest, FilterRouteResponse}
import io.logbee.keyscore.model.WhichValve.whichValve
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization


object FilterRoute {
  case class FilterRouteRequest(pipelineManager: ActorRef)
  case class FilterRouteResponse(filterRoute: Route)
}

class FilterRoute extends Actor with ActorLogging with Json4sSupport {

  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  override def receive: Receive = {
    case FilterRouteRequest(pipelineManager) =>
      val filterRoute = filterRoute(pipelineManager)
      sender ! FilterRouteResponse(filterRoute)
  }

  def filterRoute(pipelineManager: ActorRef): Route = {
    pathPrefix("filter") {
      pathPrefix(JavaUUID) { filterId =>
        path("pause") {
          post {
            parameter('value.as[Boolean]) { doPause =>
              onSuccess(pipelineManager ? PauseFilter(filterId, doPause)) {
                case PauseFilterResponse(state) => complete(StatusCodes.Accepted, state)
                case Failure => complete(StatusCodes.InternalServerError)
              }
            }
          }
        } ~
          path("drain") {
            post {
              parameter('value.as[Boolean]) { doDrain =>
                onSuccess(pipelineManager ? DrainFilterValve(filterId, doDrain)) {
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
                  onSuccess(pipelineManager ? InsertDatasets(filterId, datasets, where)) {
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
                onSuccess(pipelineManager ? ExtractDatasets(filterId, amount, where)) {
                  case ExtractDatasetsResponse(datasets) => complete(StatusCodes.OK, datasets)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
            }
          } ~
          path("configurations") {
            put {
              entity(as[Configuration]) { configuration =>
                onSuccess(pipelineManager ? ConfigureFilter(filterId, configuration)) {
                  case ConfigureFilterResponse(state) => complete(StatusCodes.Accepted, state)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
            } ~
              get {
                onSuccess(pipelineManager ? RequestExistingBlueprints()) {
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
              onSuccess(pipelineManager ? CheckFilterState(filterId)) {
                case CheckFilterStateResponse(state) =>
                  complete(StatusCodes.Accepted, state)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
          path("clear") {
            get {
              onSuccess(pipelineManager ? ClearBuffer(filterId)) {
                case ClearBufferResponse(state) =>
                  complete(StatusCodes.Accepted, state)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }
      }
    }
  }
}
