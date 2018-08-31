package io.logbee.keyscore.commons

import akka.actor.{Actor, ActorLogging, Props}
import io.logbee.keyscore.commons.ConfigurationManager._
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}

object ConfigurationManager {

  def apply(): Props = Props(new ConfigurationManager())

  case class CreateConfigurationRequest(configuration: Configuration)

  case class DeleteConfigurationRequest(ref: ConfigurationRef)

  case class GetConfigurationRequest(ref: ConfigurationRef)

  case class GetConfigurationResponse(configuration: Option[Configuration])
}

class ConfigurationManager extends Actor with ActorLogging {

  private val configurations = scala.collection.mutable.Map.empty[ConfigurationRef, Configuration]

  override def receive: Receive = {
    case CreateConfigurationRequest(configuration) =>
      configurations.put(configuration.ref, configuration)

    case DeleteConfigurationRequest(ref) =>
      configurations.remove(ref)

    case GetConfigurationRequest(ref) =>
      sender ! GetConfigurationResponse(configurations.get(ref))
  }
}
