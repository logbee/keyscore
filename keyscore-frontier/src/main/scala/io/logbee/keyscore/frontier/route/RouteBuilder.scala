package io.logbee.keyscore.frontier.route

import akka.actor.FSM.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages._
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages._
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages._
import io.logbee.keyscore.commons.cluster.{AgentRemovedFromCluster, RemoveAgentFromCluster, Topics}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.Frontier
import io.logbee.keyscore.frontier.app.AppInfo
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager.{QueryAgents, QueryAgentsResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.{AgentCapabilitiesManager, ClusterPipelineManager}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager.{RequestExistingBlueprints, RequestExistingPipelines}
import io.logbee.keyscore.frontier.cluster.pipeline.subordinates.PipelineDeployer.{BlueprintResolveFailure, NoAvailableAgents, PipelineDeployed}
import io.logbee.keyscore.frontier.route.RouteBuilder.{BuildFullRoute, RouteBuilderInitialized, RouteResponse}
import io.logbee.keyscore.model.AgentModel
import io.logbee.keyscore.model.WhichValve.whichValve
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

/**
  * The RouteBuilder combines multiple routes to one specific server route for the Frontier.
  */
object RouteBuilder {

  case object RouteBuilderInitialized

  case object BuildFullRoute

  case class RouteResponse(route: Flow[HttpRequest, HttpResponse, Any])

  def apply(clusterAgentManager: ActorRef): Props = {
    Props(new RouteBuilder(clusterAgentManager))
  }
}

class RouteBuilder(aM: ActorRef) extends Actor with ActorLogging with Json4sSupport with RouteImplicits {

  case class RouteBuilderState(configurationManager: ActorRef = null, blueprintManager: ActorRef = null, descriptorManager: ActorRef = null) {
    def isComplete: Boolean = configurationManager != null && blueprintManager != null && descriptorManager != null
  }

  val appInfo = AppInfo(classOf[Frontier])

  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val mediator = DistributedPubSub(context.system).mediator

  private val corsSettings = CorsSettings.defaultSettings.copy(
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS),
    allowedOrigins = HttpOriginRange.*,
    allowedHeaders = HttpHeaderRange.*
  )

  val settings = cors(corsSettings)

  private var route = pathSingleSlash {
    complete {
      appInfo
    }
  }

  private val clusterAgentManager = aM
  private var blueprintManager = null
  private val clusterPipelineManager = system.actorOf(ClusterPipelineManager(clusterAgentManager))

  override def preStart(): Unit = {
    mediator ! Publish(Topics.WhoIsTopic, WhoIs(ConfigurationService))
    mediator ! Publish(Topics.WhoIsTopic, WhoIs(DescriptorService))
    mediator ! Publish(Topics.WhoIsTopic, WhoIs(BlueprintService))
    context.become(initializing(RouteBuilderState()))
  }

  override def receive: Receive = {
    case _ =>
      log.error("Illegal State")
  }

  private def initializing(state: RouteBuilderState): Receive = {
    case HereIam(BlueprintService, ref) =>
      maybeRunning(state.copy(blueprintManager = ref))
      this.route = this.route ~ blueprintResourceRoute(ref)
    case HereIam(ConfigurationService, ref) =>
      maybeRunning(state.copy(configurationManager = ref))
      this.route = this.route ~ configurationResources(ref)
    case HereIam(DescriptorService, ref) =>
      maybeRunning(state.copy(descriptorManager = ref))
      this.route = this.route ~ descriptorResources(ref)
  }

  private def maybeRunning(state: RouteBuilderState): Unit = {
    if (state.isComplete) {
      context.become(running(state))
      context.parent ! RouteBuilderInitialized
    }
    else {
      context.become(initializing(state))
    }
  }

  private def running(state: RouteBuilderState): Receive = {
    case BuildFullRoute =>
      log.debug("Routes built.")
      val r = buildFullRoute
      sender ! RouteResponse(r)
  }

  private def buildFullRoute: Route = {
    val fullRoute = route ~ pipelineRoute(blueprintManager) ~ filterRoute ~ agentsRoute
    settings { fullRoute }
  }

  //Defining all the routes
  //TODO Handle this with the new Route Helper Classes

  def pipelineRoute(blueprintManager: ActorRef): Route = {
    pathPrefix("pipeline") {
      pathPrefix("configuration") {
        pathPrefix("*") {
          get {
            onSuccess(clusterPipelineManager ? RequestExistingBlueprints()) {
              case PipelineBlueprintsResponse(listOfConfigurations) => complete(StatusCodes.OK, listOfConfigurations)
              case _ => complete(StatusCodes.InternalServerError)
            }
          } ~
            delete {
              clusterPipelineManager ! ClusterPipelineManager.DeleteAllPipelines
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
                clusterPipelineManager ! ClusterPipelineManager.DeletePipeline(id = configId)
                complete(StatusCodes.OK)
              }
          } ~
          put {
            entity(as[BlueprintRef]) { blueprintRef =>
              onSuccess(clusterPipelineManager ? ClusterPipelineManager.CreatePipeline(blueprintRef)) {
                case NoAvailableAgents => complete(StatusCodes.NoContent)
                case BlueprintResolveFailure => complete(StatusCodes.Conflict)
                case PipelineDeployed  => complete(StatusCodes.Created)
                case _ => complete(StatusCodes.InternalServerError)
              }
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
              onSuccess(clusterPipelineManager ? RequestExistingPipelines()) {
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
                  onSuccess(clusterPipelineManager ? RequestExistingPipelines()) {
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

  def filterRoute: Route = {
    pathPrefix("filter") {
      pathPrefix(JavaUUID) { filterId =>
        path("pause") {
          post {
            parameter('value.as[Boolean]) { doPause =>
              onSuccess(clusterPipelineManager ? PauseFilter(filterId, doPause)) {
                case PauseFilterResponse(state) => complete(StatusCodes.Accepted, state)
                case Failure => complete(StatusCodes.InternalServerError)
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
          }
      }
    }
  }

  def agentsRoute: Route = {
    pathPrefix("agent") {
      pathPrefix(JavaUUID) { agentID =>
        delete {
          onSuccess(clusterAgentManager ? RemoveAgentFromCluster(agentID)) {
            case AgentRemovedFromCluster(agentID) => complete(StatusCodes.OK)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      } ~
        get {
          onSuccess(clusterAgentManager ? QueryAgents) {
            case QueryAgentsResponse(agents) => complete(StatusCodes.OK, agents.map(agent => AgentModel(agent.id.toString, agent.name, agent.ref.path.address.host.get)))
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
    }
  }

  def blueprintResourceRoute(blueprintManager: ActorRef): Route = {
    pathPrefix("resources") {
      pathPrefix("blueprint") {
        pathPrefix("pipeline") {
          pathPrefix("*") {
            get {
              onSuccess(blueprintManager ? GetAllPipelineBlueprintsRequest) {
                case GetAllPipelineBlueprintsResponse(pipelineBlueprints) => complete(StatusCodes.OK, pipelineBlueprints)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
            pathPrefix(JavaUUID) { pipelineBlueprintId =>
              post {
                entity(as[PipelineBlueprint]) { pipelineBlueprint =>
                  onSuccess(blueprintManager ? UpdatePipelineBlueprintRequest(pipelineBlueprint)) {
                    case UpdatePipelineBlueprintResponseSuccess => complete(StatusCodes.OK)
                    case _ => complete(StatusCodes.NoContent)
                  }
                }
              } ~
                put {
                  entity(as[PipelineBlueprint]) { pipelineBlueprint =>
                    onSuccess(blueprintManager ? StorePipelineBlueprintRequest(pipelineBlueprint)) {
                      case StorePipelineBlueprintResponse => complete(StatusCodes.Created)
                      case _ => complete(StatusCodes.InternalServerError)
                    }
                  }
                } ~
                get {
                  onSuccess((blueprintManager ? GetPipelineBlueprintRequest(BlueprintRef(pipelineBlueprintId.toString))).mapTo[GetPipelineBlueprintResponse]) {
                    case GetPipelineBlueprintResponse(blueprint) => complete(StatusCodes.OK, blueprint)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                } ~
                delete {
                  onSuccess(blueprintManager ? DeletePipelineBlueprintRequest(BlueprintRef(pipelineBlueprintId.toString))) {
                    case DeletePipelineBlueprintResponse => complete(StatusCodes.OK)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
            }
        } ~
          pathPrefix(JavaUUID) { blueprintId =>
            post {
              entity(as[SealedBlueprint]) { blueprint =>
                onSuccess(blueprintManager ? UpdateBlueprintRequest(blueprint)) {
                  case UpdateBlueprintResponseSuccess => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.NoContent)
                }
              }
            } ~
              put {
                entity(as[SealedBlueprint]) { blueprint =>
                  onSuccess(blueprintManager ? StoreBlueprintRequest(blueprint)) {
                    case StoreBlueprintResponse => complete(StatusCodes.Created)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              } ~
              get {
                onSuccess((blueprintManager ? GetBlueprintRequest(BlueprintRef(blueprintId.toString))).mapTo[GetBlueprintResponse]) {
                  case GetBlueprintResponse(blueprint) => complete(StatusCodes.OK, blueprint)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              } ~
              delete {
                onSuccess(blueprintManager ? DeleteBlueprintRequest(BlueprintRef(blueprintId.toString))) {
                  case DeleteBlueprintResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
          } ~
          pathPrefix("*") {
            get {
              onSuccess(blueprintManager ? GetAllBlueprintsRequest) {
                case GetAllBlueprintsResponse(blueprints) => complete(StatusCodes.OK, blueprints)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          }
      }
    }
  }

  def configurationResources(configurationManager: ActorRef) = {
    pathPrefix("resources") {
      pathPrefix("configuration") {
        pathPrefix("*") {
          get {
            onSuccess(configurationManager ? GetAllConfigurationRequest) {
              case GetAllConfigurationResponse(configurations) => complete(StatusCodes.OK, configurations)
              case _ => complete(StatusCodes.InternalServerError)
            }
          } ~
            delete{
              onSuccess(configurationManager ? DeleteAllConfigurationsRequest) {
                case DeleteAllConfigurationsResponse => complete(StatusCodes.OK)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
        } ~
        pathPrefix(JavaUUID) { configurationId =>
          post {
            entity(as[Configuration]) { configuration =>
              onSuccess(configurationManager ? UpdateConfigurationRequest(configuration)) {
                case UpdateConfigurationSuccessResponse => complete(StatusCodes.OK)
                case _ => complete(StatusCodes.NoContent)
              }
            }
          } ~
          put {
            entity(as[Configuration]) { configuration =>
              onSuccess(configurationManager ? StoreConfigurationRequest(configuration)) {
                case StoreConfigurationResponse => complete(StatusCodes.Created)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
          } ~
            get {
              onSuccess((configurationManager ? GetConfigurationRequest(ConfigurationRef(configurationId.toString))).mapTo[GetConfigurationResponse]) {
                case GetConfigurationResponse(configuration) => complete(StatusCodes.OK, configuration)
                case _ => complete(StatusCodes.InternalServerError)
              }
            } ~
            delete {
              onSuccess(configurationManager ? DeleteConfigurationRequest(ConfigurationRef(configurationId.toString))) {
                case DeleteConfigurationResponse => complete(StatusCodes.OK)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
        }
      }
    }
  }

  def descriptorResources(descriptorManager: ActorRef): Route = {
    pathPrefix("resources") {
      pathPrefix("descriptor") {
        pathPrefix("*") {
          get {
            onSuccess(descriptorManager ? GetAllDescriptorsRequest) {
              case GetAllDescriptorsResponse(descriptors) => complete(StatusCodes.OK, descriptors)
              case _ => complete(StatusCodes.InternalServerError)
            }
          } ~
            delete{
              onSuccess(descriptorManager ? DeleteAllDescriptorsRequest) {
                case DeleteAllDescriptorsResponse => complete(StatusCodes.OK)
                case _ => complete(StatusCodes.InternalServerError)
              }
            }
        } ~
          pathPrefix(JavaUUID) { descriptorId =>
            post {
              entity(as[Descriptor]) { descriptor =>
                onSuccess(descriptorManager ? UpdateDescriptorRequest(descriptor)) {
                  case UpdateDescriptorSuccessResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.NoContent)
                }
              }
            } ~
              put {
                entity(as[Descriptor]) { descriptor =>
                  onSuccess(descriptorManager ? StoreDescriptorRequest(descriptor)) {
                    case StoreDescriptorResponse => complete(StatusCodes.Created)
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              } ~
              get {
                onSuccess((descriptorManager ? GetDescriptorRequest(DescriptorRef(descriptorId.toString))).mapTo[GetDescriptorResponse]) {
                  case GetDescriptorResponse(descriptor) => complete(StatusCodes.OK, descriptor)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              } ~
              delete {
                onSuccess(descriptorManager ? DeleteDescriptorRequest(DescriptorRef(descriptorId.toString))) {
                  case DeleteDescriptorResponse => complete(StatusCodes.OK)
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
          }
      }
    }
  }
}
