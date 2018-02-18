package io.logbee.keyscore.frontier.app

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.{headers, _}
import akka.http.scaladsl.model.headers.{HttpOrigin, HttpOriginRange}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import io.logbee.keyscore.frontier.cluster.AgentManager
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.frontier.filters.GrokFilterConfiguration
import io.logbee.keyscore.frontier.stream.FilterDescriptorManager.{GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.stream.StreamManager._
import io.logbee.keyscore.frontier.stream.{FilterDescriptorManager, StreamManager}
import io.logbee.keyscore.model.StreamModel
import streammanagement.FilterManager
import streammanagement.FilterManager.{FilterNotFound, FilterUpdated, UpdateFilter}
import akka.http.scaladsl.model.HttpMethods._

import scala.concurrent.Await
import scala.concurrent.duration._


object FrontierApplication extends App with FrontierJsonProtocol {

  val appInfo = AppInfo(classOf[FrontierApplication])
  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 5.seconds
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val configuration = FrontierConfigProvider(system)
  val agentManager = system.actorOf(Props(classOf[AgentManager]), "AgentManager")
  val filterManager = system.actorOf(FilterManager.props)
  val streamManager = system.actorOf(StreamManager.props(filterManager))
  val filterDescriptorManager = system.actorOf(FilterDescriptorManager.props())

  val corsSettings = if (configuration.devMode) CorsSettings.defaultSettings.copy(
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS)
  ) else CorsSettings.defaultSettings.copy(
    allowedOrigins = HttpOriginRange(HttpOrigin("http://" + configuration.managerHostname + ":" + configuration.managerPort)),
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS)
  )

  val route = cors(corsSettings) {
    pathPrefix("stream") {
      path(JavaUUID) { streamId =>
        put {
          entity(as[StreamModel]) { stream =>
            onSuccess(streamManager ? CreateNewStream(streamId, stream)) {
              case StreamCreatedWithID(id) => complete(StatusCodes.Created, s"Stream '$id' created ")
              case StreamUpdated(id) => complete(StatusCodes.OK, s"Stream '$id' updated")
              case _ => complete(StatusCodes.InternalServerError)
            }
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
      } ~
      pathPrefix("descriptors") {
        get {
          onSuccess(filterDescriptorManager ? GetStandardDescriptors) {
            case StandardDescriptors(listOfDescriptors) => complete(StatusCodes.OK, listOfDescriptors)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      } ~
      pathPrefix("agents") {
        get {
          onSuccess(agentManager ? QueryAgents) {
            case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      } ~
      pathSingleSlash {
        complete {
          appInfo
        }
      }
  }

  val bindingFuture = Http().bindAndHandle(route, configuration.bindAddress, configuration.port)

  println(s"Server online at http://${configuration.bindAddress}:${configuration.port}/")

  Await.ready(system.whenTerminated, Duration.Inf)

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}

class FrontierApplication {

}