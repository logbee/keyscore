package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.{GetAllConfigurationRequest, _}
import io.logbee.keyscore.commons.cluster.resources._
import io.logbee.keyscore.commons.{ConfigurationService, HereIam, WhoIs}
import io.logbee.keyscore.model.configuration.ConfigurationRepository.{DivergedException, UnknownConfigurationException, UnknownRevisionException}
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef, ConfigurationRepository}

import scala.util.{Failure, Success, Try}

/**
  * The '''ConfigurationManager''' holds a map for all `Configurations` and <br>
  * resolves a ConfigurationRef to the specific Configuration.
  *
  * @todo Error Handling
  *
  */
object ConfigurationManager {

  def apply(): Props = Props(new ConfigurationManager())
}

class ConfigurationManager extends Actor with ActorLogging {

  private val configurations = scala.collection.mutable.Map.empty[ConfigurationRef, Configuration]
  private val repository = new ConfigurationRepository()

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
    log.debug(s" started.")
  }

  override def postStop(): Unit = {
    log.debug(s" stopped.")
  }

  override def receive: Receive = {

    case CommitConfiguration(configuration) =>
      Try(repository.commit(configuration)) match {
        case Success(ref) => sender ! CommitConfigurationSuccess(ref)
        case Failure(exception) => log.error(exception, s"Failed to commit configuration: $configuration")
        case _ => log.error(s"Failed to commit configuration: $configuration")
      }

    case ResetConfiguration(ref) =>
      Try(repository.reset(ref)) match {
        case Success(_) => sender ! ResetConfigurationSuccess()
        case Failure(exception: UnknownConfigurationException) =>
          sender ! ConfigurationNotFoundFailure(ref)
          log.error(exception, s"Failed to reset configuration: $ref")
        case Failure(exception: UnknownRevisionException) =>
          sender ! ConfigurationRevisionNotFoundFailure(ref)
          log.error(exception, s"Failed to reset configuration: $ref")
        case _ => log.error(s"Failed to reset configuration: $ref")
      }

    case RevertConfiguration(ref) =>
      Try(repository.revert(ref)) match {
        case Success(result) => sender ! RevertConfigurationSuccess(result)
        case Failure(DivergedException(base, theirs, yours)) =>
          sender ! ConfigurationDivergedFailure(base, theirs, yours)
        case Failure(exception: UnknownConfigurationException) =>
          sender ! ConfigurationNotFoundFailure(ref)
          log.error(exception, s"Failed to revert configuration: $ref")
        case Failure(exception: UnknownRevisionException) =>
          sender ! ConfigurationRevisionNotFoundFailure(ref)
          log.error(exception, s"Failed to revert configuration: $ref")
        case _ => log.error(s"Failed to revert configuration: $ref")
      }

    case RemoveConfiguration(ref) =>
      Try(repository.remove(ref)) match {
        case _ => sender ! RemoveConfigurationSuccess(ref)
      }

    case RemoveConfigurations() =>
      Try(repository.clear()) match {
        case _ => sender ! RemoveConfigurationsSuccess()
      }

    case RequestConfigurationHeadRevision(ref) =>
      sender ! ConfigurationResponse(repository.head(ref))

    case RequestAllConfigurationsHeadRevision() =>
      sender ! ConfigurationsResponse(repository.head())

    case RequestConfigurationRevision(ref) =>
      sender ! ConfigurationResponse(repository.get(ref))

    case RequestAllConfigurationRevisions(ref) =>
      sender ! ConfigurationsResponse(repository.all(ref))

    case RequestAllConfigurationsRevisions() =>
      // TODO: Not yet implemented

    case StoreOrUpdateConfigurationRequest(configuration) =>
      if (configurations.contains(configuration.ref)) {
        self forward UpdateConfigurationRequest(configuration)
      } else {
        self forward StoreConfigurationRequest(configuration)
      }
    case StoreConfigurationRequest(configuration) =>
      log.debug(s"Received StoreConfigurationRequest for $configuration")
      configurations.put(configuration.ref, configuration)
      sender ! StoreConfigurationResponse

    case GetAllConfigurationRequest =>
      log.debug("Received GetAllConfigurationRequest")
      sender ! GetAllConfigurationResponse(configurations.toMap)

    case DeleteConfigurationRequest(ref) =>
      log.debug(s"DeleteConfigurationRequest for <${ref.uuid}>")
      configurations.remove(ref)
      sender ! DeleteConfigurationResponse

    case DeleteAllConfigurationsRequest =>
      log.debug("Received DeleteAllConfigurationsRequest")
      configurations.clear()
      sender ! DeleteAllConfigurationsResponse

    case GetConfigurationRequest(ref) =>
      log.debug(s"Received GetConfigurationRequest for <${ref.uuid}>")
      configurations.get(ref) match {
        case Some(configuration) =>
          log.debug(s"Get Configuration for $ref")
          sender ! GetConfigurationSuccess(configuration)
        case _ =>
          log.warning(s"Couldn't get Configuration for <${ref.uuid}>")
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
