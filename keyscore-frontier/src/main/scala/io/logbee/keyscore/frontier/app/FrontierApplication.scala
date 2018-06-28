package io.logbee.keyscore.frontier.app

import java.util.Locale

import akka.actor.FSM.Failure
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
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.cluster.PipelineManager.{RequestExistingConfigurations, RequestExistingPipelines}
import io.logbee.keyscore.frontier.cluster.{AgentManager, ClusterCapabilitiesManager, PipelineManager}
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.json4s.{FieldTypeHints, FilterConfigTypeHints, HealthSerializer}
import io.logbee.keyscore.model._
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Success


object FrontierApplication extends App with Json4sSupport {

  val appInfo = AppInfo(classOf[FrontierApplication])
  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 30.seconds
  implicit val serialization = Serialization
  implicit val formats = Serialization.formats(FieldTypeHints + FilterConfigTypeHints) ++ JavaTypesSerializers.all ++ List(HealthSerializer)

  val configuration = FrontierConfigProvider(system)
  val agentManager = system.actorOf(Props(classOf[AgentManager]), "AgentManager")
  val pipelineManager = system.actorOf(PipelineManager(agentManager))
  val filterDescriptorManager = system.actorOf(ClusterCapabilitiesManager.props())

  val corsSettings = if (configuration.devMode) CorsSettings.defaultSettings.copy(
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS),
    allowedOrigins = HttpOriginRange.*,
    allowedHeaders = HttpHeaderRange.*

  ) else CorsSettings.defaultSettings.copy(
    allowedOrigins = HttpOriginRange(HttpOrigin("http://" + configuration.managerHostname + ":" + configuration.managerPort)),
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS)
  )

  val route = cors(corsSettings) {
    pathPrefix("pipeline") {
      pathPrefix("configuration") {
        pathPrefix("*") {
          get {
            onSuccess(pipelineManager ? RequestExistingConfigurations()) {
              case PipelineConfigurationResponse(listOfConfigurations) => complete(StatusCodes.OK, listOfConfigurations)
              case _ => complete(StatusCodes.InternalServerError)
            }
          } ~
            delete {
              pipelineManager ! PipelineManager.DeleteAllPipelines
              complete(StatusCodes.OK)
          }
        } ~
          pathPrefix(JavaUUID) { configId =>
            get {
              onSuccess(pipelineManager ? RequestExistingConfigurations()) {
                case PipelineConfigurationResponse(listOfConfigurations) =>
                  listOfConfigurations.find(pipelineConfiguration => pipelineConfiguration.id == configId) match {
                    case Some(config) => complete(StatusCodes.OK, config)
                    case None => complete(StatusCodes.NotFound)
                  }
                case _ => complete(StatusCodes.InternalServerError)
              }
            } ~
              delete {
                pipelineManager ! PipelineManager.DeletePipeline(id = configId)
                complete(StatusCodes.OK)
              }
          } ~
          put {
            entity(as[PipelineConfiguration]) { pipeline =>
              pipelineManager ! PipelineManager.CreatePipeline(pipeline)
              complete(StatusCodes.Created)
            }
          } ~
          post {
            entity(as[PipelineConfiguration]) { pipeline =>
              complete(StatusCodes.NotImplemented)
            }
          }


      } ~
        pathPrefix("instance") {
          pathPrefix("*") {
            get {
              onSuccess(pipelineManager ? RequestExistingPipelines()) {
                case PipelineInstanceResponse(listOfPipelines) => complete(StatusCodes.OK, listOfPipelines)
                case _ => complete(StatusCodes.InternalServerError)
              }
            } ~
              delete {
                complete(StatusCodes.NotImplemented)
            }
          }~
          pathPrefix(JavaUUID) { instanceId =>
            put {
              complete(StatusCodes.NotImplemented)

            } ~ delete {
              complete(StatusCodes.NotImplemented)
            } ~ get {
              onSuccess(pipelineManager ? RequestExistingPipelines()) {
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
    } ~
      pathPrefix("filter") {
        pathPrefix(JavaUUID) { filterId =>
          path("pause") {
            post {
              parameter('value.as[Boolean]) { doPause =>
                onSuccess(pipelineManager ? PauseFilter(filterId, doPause)) {
                  case Success => complete(StatusCodes.Accepted)
                  case Failure => complete(StatusCodes.InternalServerError)
                }
              }
            }
          } ~
            path  ("drain") {
              post {
                parameter('value.as[Boolean]) { doDrain =>
                  onSuccess(pipelineManager ? DrainFilterValve(filterId, doDrain)) {
                    case Success => complete(StatusCodes.Accepted)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            path("insert") {
              put {
                entity(as[List[Dataset]]) { datasets =>
                  println("Frontier: Received Insert datasets" + datasets)
                  onSuccess(pipelineManager ? InsertDatasets(filterId, datasets)) {
                    case Success => complete(StatusCodes.Accepted)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            path("extract") {
              get {
                parameter('value.as[Int]) { amount =>
                  onSuccess(pipelineManager ? ExtractDatasets(filterId, amount)) {
                    case ExtractDatasetsResponse(datasets) => complete(StatusCodes.OK, datasets)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            } ~
            path("configure") {
              put {
                entity(as[FilterConfiguration]) { filterConfig =>
                  onSuccess(pipelineManager ? ConfigureFilter(filterId, filterConfig)) {
                    case Success => complete(StatusCodes.Accepted)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            }
        }
      } ~
      pathPrefix("descriptors") {
        get {
          parameters('language.as[String]) { language =>
            onSuccess(filterDescriptorManager ? GetStandardDescriptors(Locale.forLanguageTag(language))) {
              case StandardDescriptors(listOfDescriptors) => complete(StatusCodes.OK, listOfDescriptors)
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        }
      } ~
      pathPrefix("agent") {
        pathPrefix("number") {
          get {
            onSuccess(agentManager ? QueryAgents) {
              case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents.size)
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        } ~
          get {
            onSuccess(agentManager ? QueryAgents) {
              case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents.map(agent => AgentModel(agent.id.toString, agent.name, agent.ref.path.address.host.get)))
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

  println(s"Server online at http://${
    configuration.bindAddress
  }:${
    configuration.port
  }/")

  Await.ready(system.whenTerminated, Duration.Inf)

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}

class FrontierApplication {

}