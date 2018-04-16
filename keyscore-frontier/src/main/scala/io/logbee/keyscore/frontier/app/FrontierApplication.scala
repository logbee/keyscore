package io.logbee.keyscore.frontier.app

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{HttpOrigin, HttpOriginRange}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.cluster.{AgentManager, ClusterCapabilitiesManager}
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.frontier.filters.GrokFilterConfiguration
import io.logbee.keyscore.frontier.json.helper.FilterConfigTypeHints
import io.logbee.keyscore.frontier.stream.StreamManager
import io.logbee.keyscore.frontier.stream.StreamManager._
import io.logbee.keyscore.model.{AgentModel, StreamModel}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import streammanagement.FilterManager
import streammanagement.FilterManager.{BuildGraphException, FilterNotFound, FilterUpdated, UpdateFilter}

import scala.concurrent.Await
import scala.concurrent.duration._

object FrontierApplication extends App with Json4sSupport {

  val appInfo = AppInfo(classOf[FrontierApplication])
  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  // a useful timeout duration should be evaluated
  implicit val timeout: Timeout = 30.seconds
  implicit val serialization = Serialization
  //implicit val formats = DefaultFormats
  implicit val json4sUUIDformats = Serialization.formats(FilterConfigTypeHints).withTypeHintFieldName("parameterType") ++ JavaTypesSerializers.all
  //implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val configuration = FrontierConfigProvider(system)
  val filterManager = system.actorOf(FilterManager.props)
  val agentManager = system.actorOf(Props(classOf[AgentManager]), "AgentManager")
  val streamManager = system.actorOf(StreamManager.props(filterManager))
  val filterDescriptorManager = system.actorOf(ClusterCapabilitiesManager.props())

  val corsSettings = if (configuration.devMode) CorsSettings.defaultSettings.copy(
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS)
  ) else CorsSettings.defaultSettings.copy(
    allowedOrigins = HttpOriginRange(HttpOrigin("http://" + configuration.managerHostname + ":" + configuration.managerPort)),
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS)
  )

  val route = cors(corsSettings) {
    pathPrefix("stream") {
      get{
        onSuccess(streamManager ? GetAllStreams){
          case RunningStreams(listOfStreams) => complete(StatusCodes.OK, listOfStreams)
          case _ => complete(StatusCodes.InternalServerError)
        }
      } ~
      path(JavaUUID) { streamId =>
        put {
          entity(as[StreamModel]) { stream =>
            onSuccess(streamManager ? CreateNewStream(streamId, stream)) {
              case StreamCreatedWithID(id) => complete(StatusCodes.Created, s"Stream '$id' created ")
              case StreamUpdated(id) => complete(StatusCodes.OK, s"Stream '$id' updated")
              case BuildGraphException(id, stream, msg) => complete(
                StatusCodes.BadRequest, s"Was not able to create Stream: id: '$id' errormsg:'$msg' ")
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
      pathPrefix("agent") {
        get {
          onSuccess(agentManager ? QueryAgents) {
            case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents.map(agent => AgentModel(agent.memberId.toString, agent.name, agent.ref.path.address.host.get)))
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