package io.logbee.keyscore.agent.app

import java.util.UUID

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.logbee.keyscore.agent.Agent
import io.logbee.keyscore.agent.Agent.Initialize
import io.logbee.keyscore.commons.util.AppInfo.printAppInfo
import io.logbee.keyscore.commons.util.BannerPrinter.printBanner
import io.logbee.keyscore.commons.util.RandomNameGenerator

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * The '''AgentApplication''' is the Main Class in the keyscore-agent package. <br><br>
  * The AgentApplication loads the `Configuration` for the package and creates an [[io.logbee.keyscore.agent.Agent]].
  */
object AgentApplication extends App {

  implicit val timeout: Timeout = 5 seconds
  val config = ConfigFactory.load()
  val id = computeId(config)
  val name = computeName(config)

  printBanner()
  printAppInfo[AgentApplication]
  printIdAndName(id, name)

  val system = ActorSystem("keyscore", config.getConfig("production").withFallback(config))
  val agent = system.actorOf(Agent(id, name), "agent")

  agent ! Initialize

  private def computeName(config: Config): String = {
    val name = config.getString("keyscore.agent.name")

    if (name.isEmpty) {
      new RandomNameGenerator("/agents.txt").nextName()
    }
    else {
      name
    }
  }

  private def computeId(config: Config): UUID = {
    val id = config.getString("keyscore.agent.uuid")

    if (id.isEmpty) {
      UUID.randomUUID()
    }
    else {
      UUID.fromString(id)
    }
  }

  private def printIdAndName(id: UUID, name: String): Unit = {
    println(" -------------------------------------------------------")
    println(s" Agent: $name <$id>")
    println()
  }
}

class AgentApplication {

}
