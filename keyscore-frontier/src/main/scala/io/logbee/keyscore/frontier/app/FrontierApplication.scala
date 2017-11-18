package io.logbee.keyscore.frontier.app

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.logbee.keyscore.frontier.rest.api.{StreamManager, StreamService}

import scala.concurrent.duration._
import scala.io.StdIn

object FrontierApplication extends App {

  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 1.seconds

  val streamManager = system.actorOf(Props[StreamManager], "stream-manager")
  val streamService = new StreamService(streamManager)

  val routes: Route =
    streamService.route ~
    pathPrefix("filter") {
      complete(StatusCodes.OK)
    }

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())
}