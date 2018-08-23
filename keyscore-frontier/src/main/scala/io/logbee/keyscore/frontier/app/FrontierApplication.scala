package io.logbee.keyscore.frontier.app

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.frontier.app.Frontier.Init

import scala.concurrent.Await
import scala.concurrent.duration._


object FrontierApplication extends App {

  implicit val timeout: Timeout = 5 seconds

  val config = ConfigFactory.load()
  implicit val system = ActorSystem("keyscore")
  private val isOperating: Boolean = config.getBoolean("keyscore.operation-mode")
  println(s"Frontier OperatingMode is ${isOperating}")

  val frontier = system.actorOf(Props[Frontier], "frontier")

  frontier ! Init(isOperating)

  Await.ready(system.whenTerminated, Duration.Inf)

  println(" # # # Stopped FrontierApplication. # # #")

}

class FrontierApplication {
}