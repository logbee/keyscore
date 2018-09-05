package io.logbee.keyscore.frontier.cluster.resources

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.commons.{ConfigurationService, HereIam, WhoIs}
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages._
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}

object ConfigurationManager {

  def apply(): Props = Props(new ConfigurationManager())
}

class ConfigurationManager extends Actor with ActorLogging {

  private val configurations = scala.collection.mutable.Map.empty[ConfigurationRef, Configuration]

  private val mediator = DistributedPubSub(context.system).mediator


  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
  }

  override def postStop(): Unit = super.postStop()

  override def receive: Receive = {
    case StoreConfigurationRequest(configuration) =>
      configurations.put(configuration.ref, configuration)

    case DeleteConfigurationRequest(ref) =>
      configurations.remove(ref)

    case GetConfigurationRequest(ref) =>
      sender ! GetConfigurationResponse(configurations.get(ref))

    case WhoIs(ConfigurationService) =>
      sender ! HereIam(ConfigurationService, self)
  }
}