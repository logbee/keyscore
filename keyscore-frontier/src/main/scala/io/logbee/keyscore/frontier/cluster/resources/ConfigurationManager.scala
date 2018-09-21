package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.{GetAllConfigurationRequest, _}
import io.logbee.keyscore.commons.{ConfigurationService, HereIam, WhoIs}
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}

/**
  * The ConfigurationManager holds a map for all Configurations and <br>
  * resolves a ConfigurationRef to the specific Configuration.
  */
object ConfigurationManager {

  def apply(): Props = Props(new ConfigurationManager())
}

class ConfigurationManager extends Actor with ActorLogging {

  private val configurations = scala.collection.mutable.Map.empty[ConfigurationRef, Configuration]

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
    log.debug(s" started.")
  }

  override def postStop(): Unit = {
    log.debug(s" stopped.")
  }

  override def receive: Receive = {
    case StoreConfigurationRequest(configuration) =>
      log.debug(s"Received StoreConfigurationRequest for $configuration")
      configurations.put(configuration.ref, configuration)
      sender ! StoreConfigurationResponse

    case GetAllConfigurationRequest =>
      log.debug("Received GetAllConfigurationRequest")
      sender ! GetAllConfigurationResponse(configurations.toMap)

    case DeleteConfigurationRequest(ref) =>
      log.debug(s"DeleteConfigurationRequest for $ref")
      configurations.remove(ref)
      sender ! DeleteConfigurationResponse

    case DeleteAllConfigurationsRequest =>
      log.debug("Received DeleteAllConfigurationsRequest")
      configurations.clear()
      sender ! DeleteAllConfigurationsResponse

    case GetConfigurationRequest(ref) =>
      log.debug(s"Received GetConfigurationRequest for $ref")
      configurations.get(ref) match {
        case Some(configuration) =>
          log.debug(s"Get Configuration for $ref")
          sender ! GetConfigurationSuccess(configuration)
        case _ =>
          log.warning(s"Couldn't get Configuration for $ref")
          GetConfigurationFailure(ref)
      }

    case UpdateConfigurationRequest(configuration) =>
      log.debug(s"Received UpdateConfigurationRequest for $configuration")
      if (configurations.contains(configuration.ref)) {
        log.debug(s"Updated Configuration for $configuration")
        configurations.put(configuration.ref, configuration)
        sender ! UpdateConfigurationSuccessResponse
      } else {
        log.warning(s"Couldn't update Configuration for $configuration")
        sender ! UpdateConfigurationFailureResponse
      }

    case WhoIs(ConfigurationService) =>
      log.debug("Received WhoIs(ConfigurationService)")
      sender ! HereIam(ConfigurationService, self)
  }
}
