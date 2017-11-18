package io.logbee.keyscore.frontier.rest.api

import java.util.UUID

import akka.actor.ActorRef
import akka.actor.Status.{Failure, Success}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import io.logbee.keyscore.frontier
import io.logbee.keyscore.frontier.rest.api.StreamManager._
import io.logbee.keyscore.frontier.{Filters, Streams}

import scala.concurrent.ExecutionContext


class StreamService(streamManager: ActorRef)
  (
    implicit val timeout: Timeout,
    implicit val executionContext: ExecutionContext
  )
  extends Directives with FrontierJsonSupport {

  val route: Route =
    pathPrefix("stream") {
      pathSingleSlash {
        post {
          entity(as[frontier.Stream]) { stream =>
            complete(StatusCodes.Created, (streamManager ? CreateStreamCommand(stream)).mapTo[UUID].map(_.toString))
            //          parameters('name.as[String], 'description.as[String]) { (name, description) =>
          }
        }
      } ~
      path("*") {
        get {
          complete((streamManager ? GetAllStreams).mapTo[Streams])
        } ~
        delete {
          onSuccess(streamManager ? DeleteAllStreamsCommand)  {
            case Success => complete(StatusCodes.OK)
          }
        }
      } ~
      path(JavaUUID) { streamId =>
        post {
          entity(as[frontier.Stream]) { stream =>
            onSuccess(streamManager ? UpdateStreamCommand(streamId, stream)) {
              case Success => complete(StatusCodes.OK)
              case Failure => complete(StatusCodes.NotFound)
            }
          }
        } ~
        get {
          onSuccess(streamManager ? GetStream(streamId)) {
            case Some(stream: frontier.Stream) => complete(stream)
            case None => complete(StatusCodes.NotFound)
          }
        } ~
        delete {
          onSuccess(streamManager ? DeleteStreamCommand(streamId))  {
            case Success => complete(StatusCodes.OK)
            case Failure => complete(StatusCodes.NotFound)
          }
        } ~
        pathSingleSlash {
          path("filter") {
            path("*") {
              get {
                complete((streamManager ? GetAllFiltersFromStream(streamId)).mapTo[Filters])
              } ~
                delete {
                  onSuccess(streamManager ? RemoveAllFiltersFromStreamsCommand)  {
                    case Success => complete(StatusCodes.OK)
                  }
                }
            } ~
              path(JavaUUID) { filterId =>
                post {
                  entity(as[frontier.Stream]) { stream =>
                    onSuccess(streamManager ? AddFilterToStreamCommand(streamId, filterId)) {
                      case Success => complete(StatusCodes.OK)
                      case Failure => complete(StatusCodes.NotFound)
                    }
                  }
                } ~
                  delete {
                    onSuccess(streamManager ? RemoveFilterFromStreamCommand(streamId, filterId))  {
                      case Success => complete(StatusCodes.OK)
                      case Failure => complete(StatusCodes.NotFound)
                    }
                  }
              }
          }
        }
      }
    }
}
