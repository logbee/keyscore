package io.logbee.keyscore.frontier.app

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, CreatePipelineOrder}
import io.logbee.keyscore.frontier.app.PipelineManager.CreatePipeline
import io.logbee.keyscore.frontier.cluster.AgentManager.QueryAgents
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.filter.MetaFilterDescriptor

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object PipelineManager {
  def props(agentManager: ActorRef): Props = Props(new PipelineManager(agentManager))

  case class CreatePipelineFailure(message: String)

  case class CreatePipeline(pipelineConfiguration: PipelineConfiguration)

}

class PipelineManager(agentManager: ActorRef) extends Actor with ActorLogging {

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  var frontierApplication: ActorRef = sender()
  var availableAgents: mutable.Map[ActorRef, List[MetaFilterDescriptor]] = mutable.Map.empty[ActorRef, List[MetaFilterDescriptor]]


  mediator ! Subscribe("agents", self)


  override def receive: Receive = {
    case CreatePipeline(pipelineConfiguration) =>
      log.info("[Frontier] Recieved CreatePipeline")
      val agentToCall = createListOfPossibleAgents(pipelineConfiguration).head
      log.info("[Frontier] Selected Agent is " + agentToCall.toString())
      agentToCall ! CreatePipelineOrder(pipelineConfiguration)

    case AgentCapabilities(metaFilterDescriptors) => {
      availableAgents.getOrElseUpdate(sender, metaFilterDescriptors)
    }
  }

  def checkIfCapabilitesMatchRequirements(pipelineConfiguration: PipelineConfiguration, agent: (ActorRef, List[MetaFilterDescriptor])): Boolean = {
    var requiredFilters: ListBuffer[String] = ListBuffer.empty

    requiredFilters += pipelineConfiguration.sink.descriptor.name
    requiredFilters += pipelineConfiguration.source.descriptor.name
    pipelineConfiguration.filter.foreach(filter => {
      requiredFilters += filter.descriptor.name
    })

    if(requiredFilters.count(filtername => agent._2.map(descriptor => descriptor.name).contains(filtername)) ==
      requiredFilters.size) {
      return true
    } else {
      log.info("")
    }
    false
  }

  def createListOfPossibleAgents(pipelineConfiguration: PipelineConfiguration): List[ActorRef] = {
    var possibleAgents: ListBuffer[ActorRef] = ListBuffer.empty
    availableAgents.foreach { agent =>
      if (checkIfCapabilitesMatchRequirements(pipelineConfiguration, agent)) {
        possibleAgents += agent._1
      } else {
        log.info("[Frontier / PipelineManager]: Agent " + agent + " doesn't match requirements.")
      }
    }
    possibleAgents.toList
  }
}
