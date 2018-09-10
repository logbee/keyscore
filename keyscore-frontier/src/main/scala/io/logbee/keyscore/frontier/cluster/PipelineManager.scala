package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.frontier.cluster.PipelineManager.{CreatePipeline, DeleteAllPipelines, RequestExistingBlueprints, RequestExistingPipelines}
import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object PipelineManager {

  case class RequestExistingPipelines()

  case class RequestExistingBlueprints()

  case class CreatePipeline(pipelineBlueprint: PipelineBlueprint)

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
  var availableAgents: mutable.Map[ActorRef, Seq[Descriptor]] = mutable.Map.empty[ActorRef, Seq[Descriptor]]

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
    case CreatePipeline(pipelineBlueprint) =>
      log.info("Received CreatePipeline")
      if (availableAgents.nonEmpty) {
        val agent = createListOfPossibleAgents(pipelineBlueprint).head
        log.info("Selected Agent is " + agent.toString())
        pipelineSchedulerSelector(agent, context) ! CreatePipelineOrder(pipelineBlueprint)
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

    case RequestExistingBlueprints() =>
      val collector = context.system.actorOf(PipelineConfigurationCollector(sender, availableAgents.keys))
      availableAgents.keys.foreach(agent => {
        pipelineSchedulerSelector(agent, context) ! RequestPipelineBlueprints(collector)
      })
  }

  def checkIfCapabilitiesMatchRequirements(requiredDescriptors: List[DescriptorRef], agent: (ActorRef, Seq[Descriptor])): Boolean = {

    if (requiredDescriptors.count(descriptorRef => agent._2.map(descriptor => descriptor.ref).contains(descriptorRef)) ==
      requiredDescriptors.size) {
      return true
    }
    false
  }

  def createListOfPossibleAgents(pipelineBlueprint: PipelineBlueprint): List[ActorRef] = {

    val requiredDescriptors = pipelineBlueprint.blueprints.foldLeft(List.empty[DescriptorRef]) {
      case (result, blueprint: SourceBlueprint) => result :+ DescriptorRef(blueprint.descriptor.uuid)
      case (result, blueprint: FilterBlueprint) => result :+ DescriptorRef(blueprint.descriptor.uuid)
      case (result, blueprint: SinkBlueprint) => result :+ DescriptorRef(blueprint.descriptor.uuid)
      case (result, blueprint: BranchBlueprint) => result :+ DescriptorRef(blueprint.descriptor.uuid)
      case (result, blueprint: MergeBlueprint) => result :+ DescriptorRef(blueprint.descriptor.uuid)
    }

    var possibleAgents: ListBuffer[ActorRef] = ListBuffer.empty
    availableAgents.foreach { agent =>
      if (checkIfCapabilitiesMatchRequirements(requiredDescriptors, agent)) {
        possibleAgents += agent._1
      } else {
        log.info(s"Agent '$agent' doesn't match requirements.")
      }
    }
    possibleAgents.toList
  }
}
