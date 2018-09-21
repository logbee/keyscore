package io.logbee.keyscore.frontier.route

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
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
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.Frontier
import io.logbee.keyscore.frontier.app.AppInfo
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager
import io.logbee.keyscore.frontier.route.RouteBuilder.{BuildFullRoute, RouteBuilderInitialized, RouteResponse}
import io.logbee.keyscore.frontier.route.routes.AgentRoute.agentsRoute
import io.logbee.keyscore.frontier.route.routes.FilterRoute._
import io.logbee.keyscore.frontier.route.routes.PipelineRoute._
import io.logbee.keyscore.frontier.route.routes.resources.BlueprintResourceRoute._
import io.logbee.keyscore.frontier.route.routes.resources.ConfigurationResourceRoute._
import io.logbee.keyscore.frontier.route.routes.resources.DescriptorResourceRoute.descriptorResourcesRoute

/**
  * The '''RouteBuilder''' combines multiple routes to one specific server route for the Frontier.
  *
  * @todo Use the new ServiceDiscovery
  */
object RouteBuilder {

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

class RouteBuilder(clusterAgentManagerRef: ActorRef) extends Actor with ActorLogging with RouteImplicits {

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

  private var mainRoute = pathSingleSlash {
    complete {
      appInfo
    }
  }

  private val clusterPipelineManager = system.actorOf(ClusterPipelineManager(clusterAgentManager))
  private val clusterAgentManager = clusterAgentManagerRef
  private var blueprintManager: ActorRef = _

  override def preStart(): Unit = {
    mediator ! Publish(Topics.WhoIsTopic, WhoIs(ConfigurationService))
    mediator ! Publish(Topics.WhoIsTopic, WhoIs(DescriptorService))
    mediator ! Publish(Topics.WhoIsTopic, WhoIs(BlueprintService))
    context.become(initializing(RouteBuilderState()))
    log.debug(s" started.")
  }

  override def postStop(): Unit = {
    log.debug(s" stopped.")
  }

  override def receive: Receive = {
    case _ =>
      log.error("Illegal State")
  }

  private def initializing(state: RouteBuilderState): Receive = {
    case HereIam(BlueprintService, ref) =>
      maybeRunning(state.copy(blueprintManager = ref))
      this.mainRoute = this.mainRoute ~ blueprintResourceRoute(ref)
      blueprintManager = ref
    case HereIam(ConfigurationService, ref) =>
      maybeRunning(state.copy(configurationManager = ref))
      this.mainRoute = this.mainRoute ~ configurationResourcesRoute(ref)
    case HereIam(DescriptorService, ref) =>
      maybeRunning(state.copy(descriptorManager = ref))
      this.mainRoute = this.mainRoute ~ descriptorResourcesRoute(ref)
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
    val fullRoute = mainRoute ~ agentsRoute(clusterAgentManager) ~ pipelineRoute(clusterPipelineManager, blueprintManager) ~ filterRoute(clusterPipelineManager)

    settings { fullRoute }
  }

}
