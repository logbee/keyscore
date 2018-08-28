package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.cluster.PipelineManager.{DeleteAllPipelines, RequestExistingConfigurations, RequestExistingPipelines}
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.descriptor.Descriptor

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object PipelineManager {

  case class RequestExistingPipelines()

  case class RequestExistingConfigurations()

  case class CreatePipeline(pipelineConfiguration: PipelineConfiguration)

  case class DeletePipeline(id: UUID)

  case object DeleteAllPipelines

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
  var availableAgents: mutable.Map[ActorRef, List[Descriptor]] = mutable.Map.empty[ActorRef, List[Descriptor]]

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)
    mediator ! Subscribe("cluster", self)
    mediator ! Publish("cluster", ActorJoin("ClusterCapManager", self))
    log.info("PipelineManager started.")
  }

  override def postStop(): Unit = {
    mediator ! Publish("cluster", ActorLeave("ClusterCapManager", self))
    mediator ! Subscribe("agents", self)
    mediator ! Unsubscribe("cluster", self)
    log.info("PipelineManager stopped.")
  }

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

    case DeleteAllPipelines =>
      availableAgents.keys.foreach(agent =>
        context.actorSelection(agent.path / "PipelineScheduler") ! DeleteAllPipelinesOrder
      )

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
        pipelineSchedulerSelector(agent, context) forward message
      })
    case message: CheckFilterState =>
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) forward message
      })
    case message: ClearBuffer =>
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) forward message
      })
    case RequestExistingPipelines() =>
      val collector = context.system.actorOf(PipelineInstanceCollector(sender, availableAgents.keys))
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) ! RequestPipelineInstance(collector)
      })

    case RequestExistingConfigurations() =>
      val collector = context.system.actorOf(PipelineConfigurationCollector(sender, availableAgents.keys))
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) ! RequestPipelineConfigurations(collector)
      })
  }

  def checkIfCapabilitiesMatchRequirements(pipelineConfiguration: PipelineConfiguration, agent: (ActorRef, List[Descriptor])): Boolean = {
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
