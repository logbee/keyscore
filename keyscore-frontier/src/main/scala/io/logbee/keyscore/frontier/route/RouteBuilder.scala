package io.logbee.keyscore.frontier.route

import java.util.Locale

import akka.actor.FSM.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.{AgentRemovedFromCluster, RemoveAgentFromCluster}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.Frontier
import io.logbee.keyscore.frontier.Frontier.BuildServer
import io.logbee.keyscore.frontier.app.AppInfo
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.cluster.PipelineManager.{RequestExistingConfigurations, RequestExistingPipelines}
import io.logbee.keyscore.frontier.cluster.{AgentManager, ClusterCapabilitiesManager, PipelineManager}
import io.logbee.keyscore.frontier.route.RouteBuilder.BuildFullRoute
import io.logbee.keyscore.model.WhichValve.whichValve
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.json4s._
import io.logbee.keyscore.model.{AgentModel, Dataset, PipelineConfiguration}
import org.json4s.native.Serialization

import scala.concurrent.duration._


object RouteBuilder {

  case object BuildFullRoute

}

class RouteBuilder extends Actor with ActorLogging with Json4sSupport {
  val appInfo = AppInfo(classOf[Frontier])

  implicit val timeout: Timeout = 30 seconds
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  val agentManager = system.actorOf(Props(classOf[AgentManager]), "AgentManager")
  val pipelineManager = system.actorOf(PipelineManager(agentManager))
  val filterDescriptorManager = system.actorOf(ClusterCapabilitiesManager.props())

  private val corsSettings = CorsSettings.defaultSettings.copy(
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS),
    allowedOrigins = HttpOriginRange.*,
    allowedHeaders = HttpHeaderRange.*
  )

  val settings = cors(corsSettings)

  val pipelineRoute = pathPrefix("pipeline") {
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
          } ~
            pathPrefix(JavaUUID) { instanceId =>
              put {
                complete(StatusCodes.NotImplemented)
              } ~
                delete {
                  complete(StatusCodes.NotImplemented)
                } ~
                get {
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
    }

  val filterRoute = pathPrefix("filter") {
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
        path("config") {
          put {
            entity(as[FilterConfiguration]) { filterConfig =>
              onSuccess(pipelineManager ? ConfigureFilter(filterId, filterConfig)) {
                case ConfigureFilterResponse(state) => complete(StatusCodes.Accepted, state)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
            get {
              onSuccess(pipelineManager ? RequestExistingConfigurations()) {
                case PipelineConfigurationResponse(listOfConfigurations) => listOfConfigurations.flatMap(_.filter).find(_.id == filterId) match {
                  case Some(filter) => complete(StatusCodes.OK, filter)
                  case None => complete(StatusCodes.NotFound
                  )
                }
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

  val descriptorsRoute = pathPrefix("descriptors") {
    get {
      parameters('language.as[String]) { language =>
        onSuccess(filterDescriptorManager ? GetStandardDescriptors(Locale.forLanguageTag(language))) {
          case StandardDescriptors(listOfDescriptors) => complete(StatusCodes.OK, listOfDescriptors)
          case _ => complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

  val agentsRoute = pathPrefix("agent") {
    pathPrefix("number") {
      get {
        onSuccess(agentManager ? QueryAgents) {
          case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents.size)
          case _ => complete(StatusCodes.InternalServerError)
        }
      }
    } ~
      pathPrefix(JavaUUID) { agentID =>
        delete {
          onSuccess(agentManager ? RemoveAgentFromCluster(agentID)) {
            case AgentRemovedFromCluster(agentID) => complete(StatusCodes.OK)
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
  }

  val infoRoute = pathSingleSlash {
    complete {
      appInfo
    }
  }

  override def receive: Receive = {
    case BuildFullRoute =>
      val serverRoute = settings { pipelineRoute ~ filterRoute ~ descriptorsRoute ~ agentsRoute ~ infoRoute}
      sender ! BuildServer(serverRoute)
  }
}
