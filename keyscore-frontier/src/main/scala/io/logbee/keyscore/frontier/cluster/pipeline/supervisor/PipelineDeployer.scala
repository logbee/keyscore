package io.logbee.keyscore.frontier.cluster.pipeline.supervisor

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.CreatePipelineOrder
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetPipelineBlueprintRequest, GetPipelineBlueprintResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.BlueprintCollector
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentCapabilitiesManager.{AgentsForPipelineRequest, AgentsForPipelineResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentStatsManager.{StatsForAgentsRequest, StatsForAgentsResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.PipelineDeployer.{BlueprintsResponse, CreatePipelineRequest, StartResolvingBlueprintRef}
import io.logbee.keyscore.model.blueprint.ToBase.sealedToDescriptor
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object PipelineDeployer {

  def apply(localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection): Props = Props(new PipelineDeployer(localPipelineManagerResolution))


  case class CreatePipelineRequest(blueprintRef: BlueprintRef, sender: ActorRef)

  case object CreatePipelineResponse

  case class BlueprintsResponse(blueprints: List[SealedBlueprint])

  case class DescriptorsResponse(descriptors: List[Descriptor])

  private case object StartResolvingBlueprintRef

}

/**
  * The PipelineDeployer gathers all the required information to schedule a pipeline on an agent.
  */
class PipelineDeployer(localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection) extends Actor with ActorLogging {

  private val mediator: ActorRef = DistributedPubSub(context.system).mediator
  private var blueprintManager: ActorRef = _
  private var descriptorManager: ActorRef = _
  private var agentStatsManager: ActorRef = _
  private var agentCapabilitiesManager: ActorRef = _
  var blueprintRef: BlueprintRef = _
  var messenger: ActorRef = _
  var blueprintForPipeline: PipelineBlueprint = _

  var descriptorRefs: ListBuffer[DescriptorRef] = mutable.ListBuffer.empty[DescriptorRef]

  case class CreatePipelineSupervisorState(blueprintManager: ActorRef = null, descriptorManager: ActorRef = null, agentStatsManager: ActorRef = null, agentCapabilitiesManager: ActorRef = null) {
    def isComplete: Boolean = blueprintManager != null && descriptorManager != null && agentStatsManager != null && agentCapabilitiesManager != null
  }

  override def receive: Receive = {

    case CreatePipelineRequest(ref, sender) =>
      mediator ! WhoIs(DescriptorService)
      mediator ! WhoIs(BlueprintService)
      mediator ! WhoIs(AgentStatsService)
      mediator ! WhoIs(AgentCapabilitiesService)
      blueprintRef = ref
      messenger = sender
      context.become(initializing(CreatePipelineSupervisorState()))
  }

  private def initializing(state: CreatePipelineSupervisorState): Receive = {
    case HereIam(BlueprintService, ref) =>
      blueprintManager = ref
      maybeRunning(state.copy(blueprintManager = ref))
    case HereIam(DescriptorService, ref) =>
      descriptorManager = ref
      maybeRunning(state.copy(descriptorManager = ref))
    case HereIam(AgentStatsService, ref) =>
      agentStatsManager = ref
      maybeRunning(state.copy(agentStatsManager = ref))
    case HereIam(AgentCapabilitiesService, ref) =>
      agentCapabilitiesManager = ref
      maybeRunning(state.copy(agentCapabilitiesManager = ref))
  }


  private def running: Receive = {

    case StartResolvingBlueprintRef =>
      blueprintManager ! GetPipelineBlueprintRequest(blueprintRef)

    case GetPipelineBlueprintResponse(blueprint) => blueprint match {
      case Some(pipelineBlueprint) => {
        context.system.actorOf(BlueprintCollector(self, pipelineBlueprint, blueprintManager))
        blueprintForPipeline = pipelineBlueprint
      }
    }

    case BlueprintsResponse(blueprints) =>
      blueprints.foreach(current => {
        descriptorRefs += current.descriptorRef
      })

      agentCapabilitiesManager ! AgentsForPipelineRequest(descriptorRefs.toList)

    case AgentsForPipelineResponse(possibleAgents) =>
      agentStatsManager ! StatsForAgentsRequest(possibleAgents)

    case StatsForAgentsResponse(possibleAgents)   =>
      val selectedAgent = scala.util.Random.shuffle(possibleAgents).head._1
      localPipelineManagerResolution(selectedAgent, context) !  CreatePipelineOrder(blueprintForPipeline)
  }

  private def maybeRunning(state: CreatePipelineSupervisorState): Unit = {
    if (state.isComplete) {
      context.become(running)
      self ! StartResolvingBlueprintRef
    }
    else {
      context.become(initializing(state))
    }
  }

}
