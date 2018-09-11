package io.logbee.keyscore.frontier.cluster.pipeline.supervisor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetPipelineBlueprintRequest, GetPipelineBlueprintResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.BlueprintCollector
import io.logbee.keyscore.frontier.cluster.pipeline.manager.AgentManager.AgentsForPipelineRequest
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.PipelineDeployer.{BlueprintsResponse, CreatePipelineRequest, StartResolvingBlueprintRef}
import io.logbee.keyscore.model.blueprint.ToBase.sealedToDescriptor
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object PipelineDeployer {

  def apply(): Props = Props(new PipelineDeployer())


  case class CreatePipelineRequest(blueprintRef: BlueprintRef, sender: ActorRef)

  case object CreatePipelineResponse

  case class BlueprintsResponse(blueprints: List[SealedBlueprint])

  case class DescriptorsResponse(descriptors: List[Descriptor])

  private case object StartResolvingBlueprintRef

}

class PipelineDeployer extends Actor with ActorLogging {

  private val mediator: ActorRef = DistributedPubSub(context.system).mediator
  private var blueprintManager: ActorRef = _
  private var descriptorManager: ActorRef = _
  private var agentManager: ActorRef = _
  var blueprintRef: BlueprintRef = _
  var messenger: ActorRef = _
  var blueprint: PipelineBlueprint = _

  var descriptorRefs: ListBuffer[DescriptorRef] = mutable.ListBuffer.empty[DescriptorRef]

  case class CreatePipelineSupervisorState(blueprintManager: ActorRef = null, descriptorManager: ActorRef = null, agentManager: ActorRef = null) {
    def isComplete: Boolean = blueprintManager != null && descriptorManager != null && agentManager != null
  }

  override def receive: Receive = {

    case CreatePipelineRequest(ref, sender) =>
      mediator ! WhoIs(DescriptorService)
      mediator ! WhoIs(BlueprintService)
      mediator ! WhoIs(AgentManagerService)
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
    case HereIam(AgentManagerService, ref) =>
      agentManager = ref
      maybeRunning(state.copy(agentManager = ref))
  }


  private def running: Receive = {

    case StartResolvingBlueprintRef =>
      blueprintManager ! GetPipelineBlueprintRequest(blueprintRef)

    case GetPipelineBlueprintResponse(blueprint) => blueprint match {
      case Some(pipelineBlueprint) => {
        context.system.actorOf(BlueprintCollector(self, pipelineBlueprint, blueprintManager))
      }
    }

    case BlueprintsResponse(blueprints) =>
      blueprints.foreach(current => {
        descriptorRefs += current.descriptorRef
      })

      agentManager ! AgentsForPipelineRequest(self, descriptorRefs.toList)


    //    case GetAgentForPiplineResonse(agentref) =>
    //      pipelineSchedulerSelector(agent, context) ! CreatePipelineOrder(pipelineBlueprint)

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
