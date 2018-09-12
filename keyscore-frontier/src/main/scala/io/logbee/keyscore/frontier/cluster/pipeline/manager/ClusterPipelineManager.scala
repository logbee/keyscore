package io.logbee.keyscore.frontier.cluster.pipeline.manager

import java.util.UUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.frontier.cluster.pipeline.manager.ClusterPipelineManager.CreatePipeline
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.PipelineDeployer
import io.logbee.keyscore.frontier.cluster.pipeline.supervisor.PipelineDeployer.CreatePipelineRequest
import io.logbee.keyscore.model.blueprint._


object ClusterPipelineManager {

  case class RequestExistingPipelines()

  case class RequestExistingBlueprints()

  case class CreatePipeline(pipelineBlueprint: PipelineBlueprint)
  case class DeletePipeline(id: UUID)

  case object DeleteAllPipelines

  def apply(clusterAgentManager: ActorRef): Props = {
    Props(new ClusterPipelineManager(
      clusterAgentManager,
      (ref, context) => context.actorSelection(ref.path / "LocalPipelineManager")
    ))
  }

  def apply(clusterAgentManager: ActorRef, localPipelineManager: (ActorRef, ActorContext) => ActorSelection): Props = {
    Props(new ClusterPipelineManager(clusterAgentManager, localPipelineManager))
  }
}

/**
  * The ClusterPipelineManager<br>
  * - starts the PipelineDeployer and send the  CreatePipeline Message with the BlueprintRef
  * @param clusterAgentManager
  * @param localPipelineManagerResolution
  */
class ClusterPipelineManager(clusterAgentManager: ActorRef, localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection) extends Actor with ActorLogging {

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  var agentStatsManager: ActorRef = _

  override def preStart(): Unit = {
    mediator ! Subscribe("agents", self)
    mediator ! Subscribe("cluster", self)
    mediator ! Publish("cluster", ActorJoin("ClusterCapManager", self))
    mediator ! WhoIs(AgentStatsService)
    log.info("ClusterPipelineManager started.")
  }

  override def postStop(): Unit = {
    mediator ! Publish("cluster", ActorLeave("ClusterCapManager", self))
    mediator ! Subscribe("agents", self)
    mediator ! Unsubscribe("cluster", self)
    log.info("ClusterPipelineManager stopped.")
  }

  override def receive: Receive = {
    case HereIam(AgentStatsService, ref) =>
      agentStatsManager = ref
      context.become(running)
  }

  private def running: Receive = {
    case CreatePipeline(pipelineBlueprint) =>
      val pipelineDeployer = context.actorOf(PipelineDeployer(localPipelineManagerResolution))
      log.info("Received CreatePipelineRequest")
      pipelineDeployer ! CreatePipelineRequest(pipelineBlueprint.ref, sender)
  }

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
