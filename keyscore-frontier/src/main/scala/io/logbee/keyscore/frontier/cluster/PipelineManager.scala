package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.{AgentCapabilities, AgentLeaved, CreatePipelineOrder, DeletePipelineOrder}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.cluster.PipelineManager.{RequestExistingPipelines}
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.filter.MetaFilterDescriptor

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object PipelineManager {

  case class RequestExistingPipelines()
  case class CreatePipeline(pipelineConfiguration: PipelineConfiguration)

  case class DeletePipeline(id: UUID)

  def apply(agentManager: ActorRef): Props = {
    Props(new PipelineManager(
      agentManager,
      (ref, context) => context.actorSelection(ref.path / "PipelineScheduler")
    ))
  }

  def apply(agentManager: ActorRef, pipelineSchedulerSelector: (ActorRef, ActorContext) => ActorSelection): Props = {
    Props(new PipelineManager(agentManager, pipelineSchedulerSelector))
  }
}

class PipelineManager(agentManager: ActorRef, pipelineSchedulerSelector: (ActorRef, ActorContext) => ActorSelection) extends Actor with ActorLogging {

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  var availableAgents: mutable.Map[ActorRef, List[MetaFilterDescriptor]] = mutable.Map.empty[ActorRef, List[MetaFilterDescriptor]]

  mediator ! Subscribe("agents", self)

  override def receive: Receive = {
    case PipelineManager.CreatePipeline(pipelineConfiguration) =>
      log.info("Received CreatePipeline")
      if (availableAgents.nonEmpty) {
        val agent = createListOfPossibleAgents(pipelineConfiguration).head
        log.info("Selected Agent is " + agent.toString())
        pipelineSchedulerSelector(agent, context) ! CreatePipelineOrder(pipelineConfiguration)
      } else {
        log.error("No Agent available")
      }

    case PipelineManager.DeletePipeline(id) =>
      availableAgents.keys.foreach(agent => {
        context.actorSelection(agent.path / "PipelineScheduler") ! DeletePipelineOrder(id)
      })

    case AgentCapabilities(metaFilterDescriptors) =>
      availableAgents.getOrElseUpdate(sender, metaFilterDescriptors)

    case AgentLeaved(ref) =>
      availableAgents.remove(ref)

    case message: PauseFilter =>
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) forward message
      })

    case message: DrainFilterValve =>
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) forward message
      })

    case message: InsertDatasets =>
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) forward message
      })

    case message: ExtractDatasets =>
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) forward message
      })

    case message: ConfigureFilter =>
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent,context) forward message
      })

    case  RequestExistingPipelines =>
      val aggregator = context.system.actorOf(PipelineStateAggregator(sender,availableAgents.keys))
      availableAgents.keys.foreach( agent => {
        pipelineSchedulerSelector(agent, context) ! RequestPipelineState(aggregator)
      })
  }

  def checkIfCapabilitiesMatchRequirements(pipelineConfiguration: PipelineConfiguration, agent: (ActorRef, List[MetaFilterDescriptor])): Boolean = {
    var requiredFilters: ListBuffer[String] = ListBuffer.empty

    requiredFilters += pipelineConfiguration.sink.descriptor.name
    requiredFilters += pipelineConfiguration.source.descriptor.name
    pipelineConfiguration.filter.foreach(filter => {
      requiredFilters += filter.descriptor.name
    })

    if (requiredFilters.count(filtername => agent._2.map(descriptor => descriptor.name).contains(filtername)) ==
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
      if (checkIfCapabilitiesMatchRequirements(pipelineConfiguration, agent)) {
        possibleAgents += agent._1
      } else {
        log.info(s"Agent '$agent' doesn't match requirements.")
      }
    }
    possibleAgents.toList
  }
}
