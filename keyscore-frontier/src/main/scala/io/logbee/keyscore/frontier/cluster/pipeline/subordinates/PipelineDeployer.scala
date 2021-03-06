package io.logbee.keyscore.frontier.cluster.pipeline.subordinates

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.{CreatePipelineOrder, Topics}
import io.logbee.keyscore.commons.cluster.resources.BlueprintMessages.{GetPipelineBlueprintRequest, GetPipelineBlueprintResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.BlueprintCollector
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.BlueprintCollector.{BlueprintsCollectorResponse, BlueprintsCollectorResponseFailure}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.AgentCapabilitiesManager.{AgentsForPipelineRequest, AgentsForPipelineResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.AgentStatsManager.{StatsForAgentsRequest, StatsForAgentsResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.subordinates.PipelineDeployer._
import io.logbee.keyscore.model.blueprint.ToBase.sealedToDescriptor
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.commons.util.ServiceDiscovery.discover

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}


object PipelineDeployer {

  def apply(localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection): Props = Props(new PipelineDeployer(localPipelineManagerResolution))

  case class CreatePipelineRequest(blueprintRef: BlueprintRef)

  case object CreatePipelineResponse

  case class DescriptorsResponse(descriptors: List[Descriptor])

  case object NoAvailableAgents

  case object PipelineDeployed

  case object BlueprintResolveFailure

  private case object StartResolvingBlueprintRef

}

/**
  * The '''PipelineDeployer''' gathers all the required information to schedule a `Pipeline` on an agent.
  *
  * @todo Error Handling
  */
class PipelineDeployer(localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection) extends Actor with ActorLogging {

  private implicit val ec = context.dispatcher

  private val mediator: ActorRef = DistributedPubSub(context.system).mediator
  private var blueprintManager: ActorRef = _
  private var agentStatsManager: ActorRef = _
  private var agentCapabilitiesManager: ActorRef = _
  var blueprintRef: BlueprintRef = _
  var routeBuilderRef: ActorRef = _
  var blueprintForPipeline: PipelineBlueprint = _

  var descriptorRefs: ListBuffer[DescriptorRef] = mutable.ListBuffer.empty[DescriptorRef]

  override def preStart(): Unit = {
    log.info(s" started.")
  }

  override def postStop(): Unit = {
    log.info(s" stopped.")
  }

  override def receive: Receive = {

    case CreatePipelineRequest(ref) =>
      blueprintRef = ref
      routeBuilderRef = sender

      discover(Seq(BlueprintService, AgentStatsService, AgentCapabilitiesService)).onComplete {
        case Success(services) =>
          blueprintManager = services(BlueprintService)
          agentStatsManager = services(AgentStatsService)
          agentCapabilitiesManager = services(AgentCapabilitiesService)
          context.become(running)
          self ! StartResolvingBlueprintRef

        case Failure(exception) =>
          log.error(exception, "Couldn't retrieve services.")
        // TODO: Handle discover errors!
      }
  }

  private def running: Receive = {
    case StartResolvingBlueprintRef =>
      log.debug(s"Start resolving BlueprintRef <${blueprintRef.uuid}>")
      blueprintManager ! GetPipelineBlueprintRequest(blueprintRef)

    case GetPipelineBlueprintResponse(blueprint) => blueprint match {
      case Some(pipelineBlueprint) => {
        log.debug(s"Starting BlueprintCollector for <${pipelineBlueprint.ref.uuid}>")
        context.actorOf(BlueprintCollector(pipelineBlueprint, blueprintManager))
        blueprintForPipeline = pipelineBlueprint
      }
      case _ =>
        log.error(s"Received unknown type of Blueprint for $blueprint.")
    }

    case BlueprintsCollectorResponse(blueprints) =>
      log.debug(s"Received list of Blueprints for <${blueprintForPipeline.ref.uuid}>")
      blueprints.foreach(current => {
        descriptorRefs += current.descriptorRef
      })
      agentCapabilitiesManager ! AgentsForPipelineRequest(descriptorRefs.toList)

    case BlueprintsCollectorResponseFailure =>
      log.error(s"Couldn't receive blueprint for <${blueprintForPipeline.ref.uuid}>")
      routeBuilderRef ! BlueprintResolveFailure
      context.stop(self)

    case AgentsForPipelineResponse(possibleAgents) =>
      log.debug(s"Received list of possible Agents: $possibleAgents")
      if (!possibleAgents.isEmpty) {
        log.debug("Requesting Stats for list of possible Agents.")
        agentStatsManager ! StatsForAgentsRequest(possibleAgents)
      } else {
        log.error(s"No available agents for Pipeline <${blueprintForPipeline.ref.uuid}>.")
        routeBuilderRef ! NoAvailableAgents
      }

    case StatsForAgentsResponse(possibleAgents) =>
      log.debug(s"Received list of possible Agents with matching stats: $possibleAgents")
      val selectedAgent = scala.util.Random.shuffle(possibleAgents).head._1
      //CreateNewPipeline
      localPipelineManagerResolution(selectedAgent, context) ! CreatePipelineOrder(blueprintForPipeline)
      log.info(s"Sent an order to create a Pipeline to the $selectedAgent")
      routeBuilderRef ! PipelineDeployed
      log.info(s"Pipeline for <${blueprintForPipeline.ref}> deployed.")
  }

}
