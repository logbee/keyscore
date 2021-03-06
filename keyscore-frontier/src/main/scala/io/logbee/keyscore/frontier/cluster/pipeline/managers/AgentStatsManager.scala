package io.logbee.keyscore.frontier.cluster.pipeline.managers

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics.AgentsTopic
import io.logbee.keyscore.commons.{AgentStatsService, HereIam, WhoIs}
import io.logbee.keyscore.commons.cluster.{AgentJoined, AgentLeaved, Topics}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.AgentStatsManager._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
  * The '''AgentStatsManager''' holds the stats for all agents in the cluster.
  */
object AgentStatsManager {
  def apply(): Props = Props(new AgentStatsManager())
  case class AgentStats(numberOfRunningPipelines: Int)
  case class StatsForAgentsRequest(possibleAgents: List[ActorRef])
  case class StatsForAgentsResponse(possibleAgents: Map[ActorRef, AgentStats])
  case object GetAvailableAgentsRequest
  case class GetAvailableAgentsResponse(availableAgents: List[ActorRef])
}

class AgentStatsManager extends Actor with ActorLogging {
  private val mediator = DistributedPubSub(context.system).mediator
  var availableAgents: ListBuffer[ActorRef] = mutable.ListBuffer.empty[ActorRef]

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
    mediator ! Subscribe(AgentsTopic, self)
    log.info(s" started")
  }

  override def postStop(): Unit = {
    log.info(s" stopped")
  }

  override def receive: Receive = {
    case AgentJoined(joinedActor) =>
      log.debug(s"Agent Joined: $joinedActor")
      availableAgents += joinedActor

    case AgentLeaved(ref) =>
      log.debug(s"Agent Left: $ref")
      availableAgents -= ref

    case GetAvailableAgentsRequest =>
      log.debug("Responding list of available Agents")
      sender ! GetAvailableAgentsResponse(availableAgents.toList)

    case StatsForAgentsRequest(requestedAgents) =>
      log.debug(s"Responding Stats for $requestedAgents")
      var statsMap = scala.collection.mutable.Map.empty[ActorRef, AgentStats]
      requestedAgents.foreach(agent => {
        statsMap.put(agent, AgentStats(-1))
      })
      sender ! StatsForAgentsResponse(statsMap.toMap)

    case WhoIs(AgentStatsService) =>
      sender ! HereIam(AgentStatsService, self)
  }


}
