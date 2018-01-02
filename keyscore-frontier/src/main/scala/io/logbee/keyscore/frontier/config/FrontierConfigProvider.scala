package io.logbee.keyscore.frontier.config

import akka.actor.{ExtendedActorSystem, ExtensionId, ExtensionIdProvider}

object FrontierConfigProvider extends ExtensionId[FrontierConfig] with ExtensionIdProvider {

  override def lookup = FrontierConfigProvider

  override def createExtension(system: ExtendedActorSystem) = FrontierConfig(system.settings.config)
}
