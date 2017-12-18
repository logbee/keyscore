package io.logbee.keyscore.frontier.app

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.frontier.config.ServerSettings
import io.logbee.keyscore.frontier.rest.api.{StreamManager, StreamService}

import scala.concurrent.duration._
import scala.io.StdIn

object FrontierApplication extends App {

  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 1.seconds

  val settings = ServerSettings(system)
  val streamManager = system.actorOf(StreamManager.props)
  val streamService = new StreamService(streamManager)


  val route=
    path("stream"){
      post{
        parameter("stream") {
          stream =>
            streamManager !
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, settings.Interface, settings.Port)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())
}