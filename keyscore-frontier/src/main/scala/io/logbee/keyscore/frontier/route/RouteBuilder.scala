package io.logbee.keyscore.frontier.route

import java.util.Locale

import akka.actor.FSM.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetAllPipelineBlueprintsRequest, GetAllPipelineBlueprintsResponse}
import io.logbee.keyscore.commons.cluster.{AgentRemovedFromCluster, RemoveAgentFromCluster, Topics}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.commons.{BlueprintService, HereIam, WhoIs}
import io.logbee.keyscore.frontier.Frontier
import io.logbee.keyscore.frontier.app.AppInfo
import io.logbee.keyscore.frontier.cluster.AgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.cluster.ClusterCapabilitiesManager.{GetStandardDescriptors, StandardDescriptors}
import io.logbee.keyscore.frontier.cluster.PipelineManager.{RequestExistingBlueprints, RequestExistingPipelines}
import io.logbee.keyscore.frontier.cluster.{ClusterCapabilitiesManager, PipelineManager}
import io.logbee.keyscore.frontier.route.RouteBuilder.{BuildFullRoute, RouteBuilderInitialized, RouteResponse}
import io.logbee.keyscore.model.AgentModel
import io.logbee.keyscore.model.WhichValve.whichValve
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization

import scala.concurrent.duration._


object RouteBuilder {

  case object RouteBuilderInitialized

  case object BuildFullRoute

  case class RouteResponse(route: Route)

  def apply(agentManager: ActorRef): Props = {
    Props(new RouteBuilder(agentManager))
  }
}

class RouteBuilder(aM: ActorRef) extends Actor with ActorLogging with Json4sSupport {

  val appInfo = AppInfo(classOf[Frontier])

  implicit val timeout: Timeout = 30 seconds
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  private val mediator = DistributedPubSub(context.system).mediator

  val agentManager = aM
  val pipelineManager = system.actorOf(PipelineManager(agentManager))
  var blueprintManager = null
  val filterDescriptorManager = system.actorOf(ClusterCapabilitiesManager.props())

  private val corsSettings = CorsSettings.defaultSettings.copy(
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS),
    allowedOrigins = HttpOriginRange.*,
    allowedHeaders = HttpHeaderRange.*
  )

  val settings = cors(corsSettings)

  def pipelineRoute(blueprintManager: ActorRef): Route = {
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
              pipelineManager ! PipelineManager.DeleteAllPipelines
              complete(StatusCodes.OK)
            }
        } ~
          pathPrefix(JavaUUID) { configId =>
            get {
              onSuccess(blueprintManager ? GetAllPipelineBlueprintsRequest) {
                case GetAllPipelineBlueprintsResponse(blueprints) =>
                  blueprints.find(blueprint => blueprint.ref.uuid == configId.toString) match {
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
            entity(as[PipelineBlueprint]) { blueprint =>
              pipelineManager ! PipelineManager.CreatePipeline(blueprint)
              complete(StatusCodes.Created)
            }
          } ~
          post {
            entity(as[PipelineBlueprint]) { blueprint =>
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
        log.info(s"Start sending a agents query with actorRef: ${self}")
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

  override def preStart(): Unit = {
    mediator ! Publish(Topics.WhoIsTopic, WhoIs(BlueprintService))
    context.become(initializing())
  }

  private def initializing(): Receive = {
    case HereIam(BlueprintService, ref) =>
      context.become(running(ref))
      context.parent ! RouteBuilderInitialized
  }

  private def running(blueprintManager: ActorRef): Receive = {
    case BuildFullRoute =>
      log.debug("Routes built.")
      sender ! RouteResponse(settings {
        pipelineRoute(blueprintManager) ~ filterRoute ~ descriptorsRoute ~ agentsRoute ~ infoRoute
      })
  }

  override def receive: Receive = {
    case _ =>
      log.error("Illegal State")
  }
}
