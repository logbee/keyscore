package io.logbee.keyscore.frontier.cluster.pipeline.manager

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentStatsManager.{AgentForPipeline, AgentsForPipelineRequest, AgentsForPipelineResponse}
import io.logbee.keyscore.model.descriptor.DescriptorRef

object AgentStatsManager {
  def apply(): Props = Props(new AgentStatsManager())

  case class AgentsForPipelineRequest(ref: ActorRef, descriptorRefs: List[DescriptorRef])
  case class AgentsForPipelineResponse(ref: ActorRef, agents: List[ActorRef])
  case class AgentForPipeline(agent: ActorRef)
}

/**
 * AgentStatsManager holds stats for all agents in the cluster and retrieves them.
 */
class AgentStatsManager extends Actor with ActorLogging {

  var agentCapabilitiesManager: ActorRef = _

  override def preStart(): Unit = {
   agentCapabilitiesManager = context.actorOf(AgentCapabilitiesManager())
  }

  override def postStop(): Unit = super.postStop()

  override def receive: Receive = {
    case AgentsForPipelineRequest(receiver, descriptorRefs) =>
      agentCapabilitiesManager ! AgentsForPipelineRequest(receiver, descriptorRefs)

    case AgentsForPipelineResponse(receiver, agents) =>
      val selectedAgent: ActorRef = chooseAgent(agents)
      receiver ! AgentForPipeline(selectedAgent)


  }

  private def chooseAgent(agents: List[ActorRef]): ActorRef = {
    scala.util.Random.shuffle(agents).head
  }

}
