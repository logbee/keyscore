package io.logbee.keyscore.frontier.config

import akka.actor.Extension
import com.typesafe.config.Config

case class FrontierConfig(config: Config) extends Extension {
  val bindAddress: String = config.getString("keyscore.frontier.bind")
  val port: Int = config.getInt("keyscore.frontier.port")
}
