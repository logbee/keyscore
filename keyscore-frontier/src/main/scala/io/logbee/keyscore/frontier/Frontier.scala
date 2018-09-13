package io.logbee.keyscore.frontier

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.Frontier._
import io.logbee.keyscore.frontier.cluster.pipeline.managers.{ClusterAgentManager, AgentStatsManager, ClusterManager}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager.{ClusterAgentManagerInitialized, Init}
import io.logbee.keyscore.frontier.cluster.resources.{BlueprintManager, ConfigurationManager, DescriptorManager}
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.frontier.route.RouteBuilder
import io.logbee.keyscore.frontier.route.RouteBuilder.{BuildFullRoute, RouteBuilderInitialized, RouteResponse}
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * The Frontier is the heart of the frontier package and starts the rest-end for the keyscore-manager or other clients so HTTP-Requests can be translated and directed to the agents.
  */
object Frontier {

  case class InitFrontier(isOperating: Boolean)

  private case object InitServices

  private case object InitRouteBuilder

  case object GetFrontierState

  case class GetFrontierStateResponse(isRunning: Boolean)

  case object StopServer

  private case class InitClusterAgentManager(isOperation: Boolean)
}

class Frontier extends Actor with ActorLogging with Json4sSupport {

  implicit val timeout: Timeout = 10 seconds
  implicit val system = context.system
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  private var configurationManager: ActorRef = _
  private var descriptorManager: ActorRef = _
  private var blueprintManager: ActorRef = _
  private var routeBuilder: ActorRef = _

  private var clusterAgentManager: ActorRef = _
  private var clusterManager: ActorRef = _

  private var agentStatsManager: ActorRef = _

  private val configuration = FrontierConfigProvider(system)

  var httpBinding: Future[Http.ServerBinding] = null

  override def preStart(): Unit = {
    log.info("Frontier started")
    configurationManager = context.actorOf(ConfigurationManager())
    descriptorManager = context.actorOf(DescriptorManager())
    blueprintManager = context.actorOf(BlueprintManager())
  }

  override def postStop(): Unit = {
    self ! StopServer
    system.terminate()
    log.info("Frontier stopped.")
  }

  override def receive: Receive = {

    case InitFrontier(isOperating) =>
      log.info("Initializing Frontier ...")

      self ! InitClusterAgentManager(isOperating)

    case InitClusterAgentManager(isOperating) =>
      clusterAgentManager = context.actorOf(Props(classOf[ClusterAgentManager]), "ClusterAgentManager")
      clusterAgentManager ! Init(isOperating)

    case ClusterAgentManagerInitialized(isOperating) =>
      clusterManager = context.actorOf(ClusterManager(clusterAgentManager), "ClusterManager")

      if(isOperating) {
        log.info("Frontier started in Running Mode.")
        context.become(running)
        self ! InitRouteBuilder
      } else {
        log.info("Frontier started in Sleeping Mode.")
        context.become(sleeping)
      }

  }

  private def running(): Receive = {
    case InitRouteBuilder =>
      routeBuilder = context.actorOf(RouteBuilder(clusterAgentManager), "RouteBuilder")

    case RouteBuilderInitialized =>
      routeBuilder ! BuildFullRoute

    case RouteResponse(route) =>
      log.info(s"Frontier Server online at http://${configuration.bindAddress}:${configuration.port}/")
      httpBinding = Http().bindAndHandle(route, configuration.bindAddress, configuration.port)

    case StopServer =>
      httpBinding
        .flatMap(_.unbind())
        .onComplete(_ => log.info("Stopped REST Server"))

    case GetFrontierState =>
      sender ! GetFrontierStateResponse(true)

  }

  private def sleeping(): Receive = {
    case GetFrontierState =>
      sender ! GetFrontierStateResponse(false)
  }
}
