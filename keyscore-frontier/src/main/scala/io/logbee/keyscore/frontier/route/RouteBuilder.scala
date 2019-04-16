package io.logbee.keyscore.frontier.route

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.metrics.MetricsManager
import io.logbee.keyscore.commons.util.{AppInfo, ServiceDiscovery}
import io.logbee.keyscore.frontier.app.FrontierApplication
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager
import io.logbee.keyscore.frontier.route.RouteBuilder.{BuildFullRoute, InitializeRouteBuilder, RouteBuilderInitialized, RouteResponse}
import io.logbee.keyscore.frontier.route.routes.{AgentRoute, FilterRoute, PipelineRoute}
import io.logbee.keyscore.frontier.route.routes.resources.{BlueprintResourceRoute, ConfigurationResourceRoute, DescriptorResourceRoute}

import scala.util.{Failure, Success}

/**
  * The '''RouteBuilder''' combines multiple routes to one specific server route for the Frontier.
  *
  */
object RouteBuilder {

  private case class InitializeRouteBuilder(configurationManager: ActorRef, descriptorManager: ActorRef, blueprintManager: ActorRef)

  case object RouteBuilderInitialized

  /**
    * Builds the full Route: AppInfo | Resources | Agent | Pipeline | Filter <br>
    */
  case object BuildFullRoute

  case class RouteResponse(route: Flow[HttpRequest, HttpResponse, Any])

  def apply(clusterAgentManager: ActorRef): Props = {
    Props(new RouteBuilder(clusterAgentManager))
  }
}

class RouteBuilder(clusterAgentManagerRef: ActorRef) extends Actor with ActorLogging with RouteImplicits with AgentRoute
  with PipelineRoute with FilterRoute with ConfigurationResourceRoute with BlueprintResourceRoute with DescriptorResourceRoute {

  val appInfo = AppInfo.fromMainClass[FrontierApplication]

  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val corsSettings = CorsSettings.defaultSettings.copy(
    allowedMethods = scala.collection.immutable.Seq(PUT, GET, POST, DELETE, HEAD, OPTIONS),
    allowedOrigins = HttpOriginRange.*,
    allowedHeaders = HttpHeaderRange.*
  )

  val settings = cors(corsSettings)

  private var mainRoute = pathSingleSlash {
    complete {
      appInfo
    }
  }

  private val clusterAgentManager = clusterAgentManagerRef
  private val clusterPipelineManager: ActorRef = system.actorOf(ClusterPipelineManager(clusterAgentManager))
  private val metricsManager: ActorRef = system.actorOf(Props[MetricsManager])
  private var blueprintManager: ActorRef = _

  override def preStart(): Unit = {
    val servicesToDiscover = Seq(ConfigurationService, DescriptorService, BlueprintService)
    ServiceDiscovery.discover(servicesToDiscover).onComplete {
      case Success(services) =>
        log.debug(s"Services retrieved: $services")
        context.become(initializing)
        self ! InitializeRouteBuilder(services(ConfigurationService), services(DescriptorService), services(BlueprintService))
      case Failure(e) =>
        log.error(e, "Couldn't retrieve necessary services.")
      // TODO: Handle discover errors!
    }
    log.debug(s" started.")
  }

  override def postStop(): Unit = {
    log.debug(s" stopped.")
  }

  override def receive: Receive = {
    case _ =>
      log.error("Illegal State")
  }

  private def initializing: Receive = {
    case InitializeRouteBuilder(configurationManagerRef, descriptorManagerRef, blueprintManagerRef) =>
      log.debug("Initializing RouteBuilder")
      blueprintManager = blueprintManagerRef
      this.mainRoute = this.mainRoute ~ blueprintResourceRoute(blueprintManagerRef)
      this.mainRoute = this.mainRoute ~ configurationResourcesRoute(configurationManagerRef)
      this.mainRoute = this.mainRoute ~ descriptorResourcesRoute(descriptorManagerRef)

      context.parent ! RouteBuilderInitialized
      context.become(running)

  }

  private def running: Receive = {
    case BuildFullRoute =>
      val r = buildFullRoute
      sender ! RouteResponse(r)
      log.debug("Routes built.")
  }

  /**
    * __Route__: AppInfo | Resources | Agent | Pipeline | Filter
    *
    * @return The complete Route for a Standard Full-Operating Frontier
    */
  private def buildFullRoute: Route = {
    val fullRoute = mainRoute ~ agentsRoute(clusterAgentManager) ~ pipelineRoute(clusterPipelineManager, blueprintManager) ~ filterRoute(clusterPipelineManager, metricsManager)

    settings { fullRoute }
  }

}
