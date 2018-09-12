package io.logbee.keyscore.frontier.cluster.pipeline.manager

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentStatsManager.{AgentStats, StatsForAgentsRequest, StatsForAgentsResponse}


/**
  * AgentStatsManager holds stats for all agents in the cluster and retrieves them.
  */
object AgentStatsManager {
  def apply(): Props = Props(new AgentStatsManager())
  case class AgentStats(numberOfRunningPipelines: Int)
  case class StatsForAgentsRequest(possibleAgents: List[ActorRef])
  case class StatsForAgentsResponse(possibleAgents: Map[ActorRef, AgentStats])

}

class AgentStatsManager extends Actor with ActorLogging {
  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
  }

  override def postStop(): Unit = super.postStop()

  override def receive: Receive = {
    case StatsForAgentsRequest(requestedAgents) =>
      var statsMap = scala.collection.mutable.Map.empty[ActorRef, AgentStats]
      requestedAgents.foreach(agent => {
        statsMap.put(agent, AgentStats(-1))
      })
      sender ! StatsForAgentsResponse(statsMap.toMap)
  }


}
