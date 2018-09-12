package io.logbee.keyscore.commons

import akka.actor.ActorRef

sealed trait Service

case object ConfigurationService extends Service
case object DescriptorService extends Service
case object BlueprintService extends Service
case object AgentCapabilitiesService extends Service
case object AgentStatsService extends Service

case class WhoIs(service: Service)

case class HereIam(service: Service, ref: ActorRef)
