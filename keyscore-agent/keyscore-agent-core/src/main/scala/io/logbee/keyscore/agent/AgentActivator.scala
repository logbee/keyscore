package io.logbee.keyscore.agent

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.osgi.framework.{BundleActivator, BundleContext}

class AgentActivator extends BundleActivator {

  val config: Config = ConfigFactory.load()
  var system: Option[ActorSystem] = None

  override def start(ctx: BundleContext): Unit = {
    system = Option(ActorSystem("keyscore", config.getConfig("production").withFallback(config)))
//    system.get.actorOf(Agent.props(ctx), "agent") ! Initialize
    system.get.actorOf(TestActor.props(ctx), "agent")
  }

  override def stop(context: BundleContext): Unit = {
    system.foreach(system => {
      system.terminate()
    })
  }
}