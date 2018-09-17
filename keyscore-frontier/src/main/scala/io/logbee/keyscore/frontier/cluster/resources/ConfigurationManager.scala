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

    log.info(s"Start-up complete: $self")
  }

  override def postStop(): Unit = {
    log.info("Stopped.")
  }

  override def receive: Receive = {
    case StoreConfigurationRequest(configuration) =>
      configurations.put(configuration.ref, configuration)
      log.info(s"DEBUG Store Configuration: $configuration")
      sender ! StoreConfigurationResponse

    case GetAllConfigurationRequest =>
      sender ! GetAllConfigurationResponse(configurations.toMap)

    case DeleteConfigurationRequest(ref) =>
      log.info(s"DEBUG Delete a config: $ref")
      configurations.remove(ref)
      sender ! DeleteConfigurationResponse

    case DeleteAllConfigurationsRequest =>
      log.info("DEBUG Delete all configs")
      configurations.clear()
      sender ! DeleteAllConfigurationsResponse

    case GetConfigurationRequest(ref) =>
      log.info(s"DEBUG Configuration Request for $ref")
      configurations.get(ref) match {
        case Some(configuration) =>
          sender ! GetConfigurationSuccess(configuration)
        case _ => GetConfigurationFailure(ref)
      }

    case UpdateConfigurationRequest(configuration) =>
      if (configurations.contains(configuration.ref)) {
        configurations.put(configuration.ref, configuration)
        sender ! UpdateConfigurationSuccessResponse
      } else {
        sender ! UpdateConfigurationFailureResponse
      }

    case WhoIs(ConfigurationService) =>
      sender ! HereIam(ConfigurationService, self)

    case e =>
      log.info(s"DEBUG Received unknown message: $e")
  }
}
