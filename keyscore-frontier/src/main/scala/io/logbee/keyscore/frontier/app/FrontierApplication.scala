package io.logbee.keyscore.frontier.app

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.commons.util.AppInfo.printAppInfo
import io.logbee.keyscore.commons.util.BannerPrinter.printBanner
import io.logbee.keyscore.frontier.Frontier
import io.logbee.keyscore.frontier.Frontier.InitFrontier

import scala.concurrent.duration._

/**
  * The '''FrontierApplication''' is the Main class of the keyscore-frontier package. <br><br>
  * The FrontierApplication loads the `Configuration` for the package and creates an [[io.logbee.keyscore.frontier.Frontier]].
  */
object FrontierApplication extends App {

  implicit val timeout: Timeout = 5 seconds

  printBanner()
  printAppInfo[FrontierApplication]

  val config = ConfigFactory.load()
  implicit val system = ActorSystem("keyscore", config.getConfig("production").withFallback(config))
  private val isOperating: Boolean = config.getBoolean("keyscore.operating-mode")
  println(s"Frontier OperatingMode is ${isOperating}")

  val frontier = system.actorOf(Props[Frontier], "frontier")

  frontier ! InitFrontier(isOperating)
}

class FrontierApplication {
}