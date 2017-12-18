package io.logbee.keyscore.frontier.config

import akka.actor.ActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import scala.concurrent.duration.Duration
import com.typesafe.config.Config
import java.util.concurrent.TimeUnit

class ServerSettingsImpl(config: Config) extends Extension {
  val Interface: String = config.getString("server.settings.interface")
  val Port:Int = config.getInt("server.settings.port")
}
object ServerSettings extends ExtensionId[ServerSettingsImpl] with ExtensionIdProvider {

  override def lookup = ServerSettings

  override def createExtension(system: ExtendedActorSystem) =
    new ServerSettingsImpl(system.settings.config)

  /**
    * Java API: retrieve the Settings extension for the given system.
    */
  override def get(system: ActorSystem): ServerSettingsImpl = super.get(system)
}
