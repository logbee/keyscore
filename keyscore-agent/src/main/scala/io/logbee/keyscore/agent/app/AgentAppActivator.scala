package io.logbee.keyscore.agent.app

import java.util.UUID

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import io.logbee.keyscore.agent.Agent
import io.logbee.keyscore.agent.Agent.Initialize
import io.logbee.keyscore.commons.util.AppInfo.printAppInfo
import io.logbee.keyscore.commons.util.BannerPrinter.printBanner
import io.logbee.keyscore.commons.util.RandomNameGenerator
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import org.osgi.framework.{BundleActivator, BundleContext}

class AgentAppActivator extends BundleActivator
{
  val config: Config = ConfigFactory.load()
  var system: Option[ActorSystem] = None
  val id = computeId(config)
  val name = computeName(config)

  override def start(ctx: BundleContext): Unit = {

    printBanner()
    printAppInfo[AgentApplication]
    printIdAndName(id, name)

    system = ActorSystem("keyscore", config.getConfig("production").withFallback(config))
    system.get.actorOf(Agent(id, name, ctx), "agent") ! Initialize
  }

  override def stop(context: BundleContext): Unit = {
    system.foreach(system => {
      system.terminate()
    })
  }

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
