package io.logbee.keyscore.frontier.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.logbee.keyscore.frontier.config.FrontierConfigProvider
import io.logbee.keyscore.frontier.stream.StreamManager
import io.logbee.keyscore.frontier.stream.StreamManager.TranslateAndCreateNewStream
import io.logbee.keyscore.model.StreamModel

import scala.concurrent.duration._
import scala.io.StdIn


object FrontierApplication extends App with FrontierJsonProtocol {

  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 1.seconds
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val configuration = FrontierConfigProvider(system)
  val streamManager = system.actorOf(StreamManager.props)

  val route =
    path("stream") {
      post {
        entity(as[StreamModel]) { stream =>
          complete((StatusCodes.Created, (streamManager ? TranslateAndCreateNewStream(stream)).map(_.toString)))
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, configuration.bindAddress, configuration.port)

  println(s"Server online at http://${configuration.bindAddress}:${configuration.port}/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())
}