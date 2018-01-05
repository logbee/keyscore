package io.logbee.keyscore.frontier.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.frontier.filters.GrokFilterConfiguration
import io.logbee.keyscore.frontier.stream.StreamManager
import io.logbee.keyscore.frontier.stream.StreamManager._
import io.logbee.keyscore.model.StreamModel
import streammanagement.FilterManager
import streammanagement.FilterManager.{FilterNotFound, FilterUpdated, UpdateFilter}

import scala.concurrent.duration._
import scala.io.StdIn


object FrontierApplication extends App with FrontierJsonProtocol {

  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 5.seconds
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val configuration = FrontierConfigProvider(system)
  val filterManager = system.actorOf(FilterManager.props)
  val streamManager = system.actorOf(StreamManager.props(filterManager))

  val route =
    pathPrefix("stream") {
      path(JavaUUID) { streamId =>
        post {
          entity(as[StreamModel]) { stream =>
            complete((StatusCodes.Created, (streamManager ? CreateNewStream(streamId, stream)).map(_.toString)))
          }
        } ~
          delete {
            onSuccess(streamManager ? DeleteStream(streamId)) {
              case StreamDeleted(id) => complete(StatusCodes.OK, s"Stream '$id' deleted")
              case StreamNotFound(id) => complete(StatusCodes.NotFound, s"Stream '$id' not found")
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
      }
    } ~
      pathPrefix("filter") {
        path(JavaUUID) { filterId =>
          put {
            entity(as[GrokFilterConfiguration]) { configuration =>
              onSuccess(filterManager ? UpdateFilter(filterId, configuration)) {
                case FilterUpdated(id) => complete(StatusCodes.OK, s"Filter '$id' updated")
                case FilterNotFound(id) => complete(StatusCodes.NotFound, s"Filter '$id' not found")
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }
        }
      }

  val bindingFuture = Http().bindAndHandle(route, configuration.bindAddress, configuration.port)

  println(s"Server online at http://${configuration.bindAddress}:${configuration.port}/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())
}