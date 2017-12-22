package io.logbee.keyscore.frontier.app

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.frontier.StreamModel
import io.logbee.keyscore.frontier.config.ServerSettings
import io.logbee.keyscore.frontier.streammanagement.StreamUtils
import streammanagement.StreamManager.{CreateNewStream, TranslateAndCreateNewStream}
import akka.pattern.ask
import streammanagement.StreamManager

import scala.concurrent.duration._
import scala.io.StdIn


object FrontierApplication extends App with FrontierJsonProtocol {

  implicit val system = ActorSystem("keyscore")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = 1.seconds
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val settings = ServerSettings(system)
  val streamManager = system.actorOf(StreamManager.props)


  val route =
    path("stream") {
      post {
        entity(as[StreamModel]) { stream =>
          complete((StatusCodes.Created, (streamManager ? TranslateAndCreateNewStream(stream)).map(_.toString)))
        }
      }

    }

  val bindingFuture = Http().bindAndHandle(route, settings.Interface, settings.Port)

  println(s"Server online at http://${settings.Interface}:${settings.Port}/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())
}