package io.logbee.keyscore.frontier

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import io.logbee.keyscore.frontier.Frontier._
import io.logbee.keyscore.frontier.cluster.{AgentManager, ClusterManager}
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.frontier.route.RouteBuilder
import io.logbee.keyscore.frontier.route.RouteBuilder.BuildFullRoute
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization

import scala.concurrent.Future
import scala.concurrent.duration._

object Frontier {

  case class Init(isOperating: Boolean)

  case class BuildServer(route: Flow[HttpRequest, HttpResponse, Any])

  case object StartServer

  case object StopServer

  case object Idle

}

class Frontier extends Actor with ActorLogging with Json4sSupport {

  implicit val timeout: Timeout = 10 seconds
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val serialization = Serialization
  implicit val formats = KeyscoreFormats.formats

  private val agentManager = system.actorOf(Props(classOf[AgentManager]), "AgentManager")
  private val clusterManager = system.actorOf(ClusterManager(agentManager), "ClusterManager")
  private val builder = system.actorOf(RouteBuilder(agentManager), "RouteBuilder")

  val configuration = FrontierConfigProvider(system)

  var httpBinding: Future[Http.ServerBinding] = null

  override def preStart(): Unit = {
    log.info("Frontier started")
  }

  override def postStop(): Unit = {
    self ! StopServer
    log.info("Frontier stopped.")
  }

  override def receive: Receive = {
    case Init(isOperating) =>
      log.info("Initializing Frontier...")
      if (isOperating) {
        self ! StartServer
      } else {
        self ! Idle
      }

    case StartServer =>
      log.info("Frontier started in Operating Mode.")
      log.info("Starting REST Server ...")

      builder ! BuildFullRoute

    case BuildServer(r) =>
      val route = r

      httpBinding = Http().bindAndHandle(route, configuration.bindAddress, configuration.port)

      log.info(s"Frontier Server online at http://${configuration.bindAddress}:${configuration.port}/")

    case StopServer =>
      httpBinding
        .flatMap(_.unbind())
        .onComplete(_ => log.info("Stopped REST Server"))

    case Idle =>
      log.info("This frontier will do nothing.")

    case _ =>
      log.info("Received unknown message.")
  }

}
