package io.logbee.keyscore.frontier.cluster.pipeline.manager

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentManager.{AgentForPipeline, AgentsForPipelineRequest, AgentsForPipelineResponse}
import io.logbee.keyscore.model.descriptor.DescriptorRef

object AgentManager {
  def apply(): Props = Props(new AgentManager())

  case class AgentsForPipelineRequest(ref: ActorRef, descriptorRefs: List[DescriptorRef])
  case class AgentsForPipelineResponse(ref: ActorRef, agents: List[ActorRef])
  case class AgentForPipeline(agent: ActorRef)
}

class AgentManager extends Actor with ActorLogging {

  private var clusterCapabilitiesManager: ActorRef = _

  override def preStart(): Unit = {
   clusterCapabilitiesManager = context.actorOf(ClusterCapabilitiesManager())
  }

  override def postStop(): Unit = super.postStop()

  override def receive: Receive = {
    case AgentsForPipelineRequest(receiver, descriptorRefs) =>
      clusterCapabilitiesManager ! AgentsForPipelineRequest(receiver, descriptorRefs)

    case AgentsForPipelineResponse(receiver, agents) =>
      val selectedAgent: ActorRef = chooseAgent(agents)
      receiver ! AgentForPipeline(selectedAgent)
  }

  private def chooseAgent(agents: List[ActorRef]): ActorRef = {
    scala.util.Random.shuffle(agents).head
  }

}
