package io.logbee.keyscore.frontier.cluster.pipeline.managers

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.{AgentJoined, Topics}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.AgentStatsManager._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
  * AgentStatsManager holds stats for all agents in the cluster and retrieves them.
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
  }

  override def postStop(): Unit = super.postStop()

  override def receive: Receive = {
    case AgentJoined(joinedActor) =>
      availableAgents = (availableAgents += joinedActor).distinct

    case GetAvailableAgentsRequest =>
      sender ! GetAvailableAgentsResponse(availableAgents.toList)

    case StatsForAgentsRequest(requestedAgents) =>
      var statsMap = scala.collection.mutable.Map.empty[ActorRef, AgentStats]
      requestedAgents.foreach(agent => {
        statsMap.put(agent, AgentStats(-1))
      })
      sender ! StatsForAgentsResponse(statsMap.toMap)
  }


}
