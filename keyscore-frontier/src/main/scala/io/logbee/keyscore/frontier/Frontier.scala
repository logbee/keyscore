package io.logbee.keyscore.frontier

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.Frontier._
import io.logbee.keyscore.frontier.cluster.AgentManager.{AgentManagerInitialized, Init}
import io.logbee.keyscore.frontier.cluster.{AgentManager, ClusterManager}
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.frontier.route.RouteBuilder
import io.logbee.keyscore.frontier.route.RouteBuilder.BuildFullRoute
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization

import scala.concurrent.Future
import scala.concurrent.duration._

object Frontier {

  case class InitFrontier(isOperating: Boolean)

  case class BuildServer(route: Flow[HttpRequest, HttpResponse, Any])

  case object StopServer

  private case object InitServer

  private case object InitSleep

  private case class InitAgentManager(isOperation: Boolean)

}

class Frontier extends Actor with ActorLogging with Json4sSupport {

  implicit val timeout: Timeout = 10 seconds
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  private var agentManager: ActorRef = null
  private var clusterManager: ActorRef = null

  private val configuration = FrontierConfigProvider(system)

  var httpBinding: Future[Http.ServerBinding] = null

  override def preStart(): Unit = {
    log.info("Frontier started")
  }

  override def postStop(): Unit = {
    self ! StopServer
    log.info("Frontier stopped.")
  }

  override def receive: Receive = {
    case InitFrontier(isOperating) =>
      log.info("Initializing Frontier ...")
      self ! InitAgentManager(isOperating)

    case InitAgentManager(isOperating) =>
      agentManager = system.actorOf(Props(classOf[AgentManager]), "AgentManager")
      agentManager ! Init(isOperating)

    case AgentManagerInitialized(isOperating) =>
      clusterManager = system.actorOf(ClusterManager(agentManager), "ClusterManager")
      if(isOperating){
        self ! InitServer
      } else {
        self ! InitSleep
      }

    case InitSleep =>
      log.info("This frontier will do nothing.")

    case InitServer =>
      log.info("Frontier started in Operating Mode.")
      log.info("Starting REST Server ...")

      val builder = system.actorOf(RouteBuilder(agentManager), "RouteBuilder")

      builder ! BuildFullRoute

    case BuildServer(r) =>
      val route = r

      httpBinding = Http().bindAndHandle(route, configuration.bindAddress, configuration.port)

      log.info(s"Frontier Server online at http://${configuration.bindAddress}:${configuration.port}/")

    case StopServer =>
      httpBinding
        .flatMap(_.unbind())
        .onComplete(_ => log.info("Stopped REST Server"))

    case _ =>
      log.info("Received unknown message.")
  }

}
