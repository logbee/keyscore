package io.logbee.keyscore.agent

import akka.actor.{Actor, ActorLogging}
import org.osgi.framework.BundleContext

class AgentExtensionBundleLoader(bundleContext: BundleContext) extends Actor with ActorLogging {

  private val config = context.system.settings.config

  override def receive: Receive = {

    case _ =>
  }
}
