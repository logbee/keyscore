package io.logbee.keyscore.frontier.cluster.pipeline.manager

import java.util.UUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.model.blueprint._


object PipelineManager {

  case class RequestExistingPipelines()

  case class RequestExistingBlueprints()

  case class CreatePipeline(pipelineBlueprint: PipelineBlueprint)

  case class DeletePipeline(id: UUID)

  case object DeleteAllPipelines

  def apply(agentClusterManager: ActorRef): Props = {
    Props(new PipelineManager(
      agentClusterManager,
      (ref, context) => context.actorSelection(ref.path / "PipelineScheduler")
    ))
  }

  def apply(agentClusterManager: ActorRef, pipelineSchedulerSelector: (ActorRef, ActorContext) => ActorSelection): Props = {
    Props(new PipelineManager(agentClusterManager, pipelineSchedulerSelector))
  }
}

class PipelineManager(agentClusterManager: ActorRef, pipelineSchedulerSelector: (ActorRef, ActorContext) => ActorSelection) extends Actor with ActorLogging {

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  var agentManager: ActorRef = _

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)
    mediator ! Subscribe("cluster", self)
    mediator ! Publish("cluster", ActorJoin("ClusterCapManager", self))
    mediator ! WhoIs(ClusterCapabilitiesService)
    log.info("PipelineManager started.")
  }

  override def postStop(): Unit = {
    mediator ! Publish("cluster", ActorLeave("ClusterCapManager", self))
    mediator ! Subscribe("agents", self)
    mediator ! Unsubscribe("cluster", self)
    log.info("PipelineManager stopped.")
  }

  override def receive: Receive = {
    case HereIam(AgentManagerService, ref) =>
      agentManager = ref
//      context.become(running)
  }

//  private def running: Receive = {
//
//  }

//  private def running: Receive = {
//    case CreatePipeline(pipelineBlueprint) =>
//      log.info("Received CreatePipeline")
//      if (availableAgents.nonEmpty) {
//        val agent = createListOfPossibleAgents(pipelineBlueprint).head
//        log.info("Selected Agent is " + agent.toString())
//        pipelineSchedulerSelector(agent, context) ! CreatePipelineOrder(pipelineBlueprint)
//      } else {
//        log.error("No Agent available")
//      }
//
//    case PipelineManager.DeletePipeline(id) =>
//      availableAgents.keys.foreach(agent => {
//        context.actorSelection(agent.path / "PipelineScheduler") ! DeletePipelineOrder(id)
//      })
//
//    case DeleteAllPipelines =>
//      availableAgents.keys.foreach(agent =>
//        context.actorSelection(agent.path / "PipelineScheduler") ! DeleteAllPipelinesOrder
//      )
//    case message: PauseFilter =>
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) forward message
//      })
//
//    case message: DrainFilterValve =>
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) forward message
//      })
//
//    case message: InsertDatasets =>
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) forward message
//      })
//
//    case message: ExtractDatasets =>
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) forward message
//      })
//
//    case message: ConfigureFilter =>
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) forward message
//      })
//    case message: CheckFilterState =>
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) forward message
//      })
//    case message: ClearBuffer =>
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) forward message
//      })
//
//
//
//    case RequestExistingPipelines() =>
//      val collector = context.system.actorOf(PipelineInstanceCollector(sender, availableAgents.keys))
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) ! RequestPipelineInstance(collector)
//      })
//
//    case RequestExistingBlueprints() =>
//      val collector = context.system.actorOf(PipelineConfigurationCollector(sender, availableAgents.keys))
//      availableAgents.keys.foreach(agent => {
//        pipelineSchedulerSelector(agent, context) ! RequestPipelineBlueprints(collector)
//      })
//  }
}
