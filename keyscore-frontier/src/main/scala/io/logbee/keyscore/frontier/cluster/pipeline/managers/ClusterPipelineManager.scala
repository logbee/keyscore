package io.logbee.keyscore.frontier.cluster.pipeline.managers

import java.util.UUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.pattern.ask
import akka.util.Timeout
import io.logbee.keyscore.commons.cluster.Paths.{LocalPipelineManagerPath, PipelineSchedulerPath}
import io.logbee.keyscore.commons.cluster.Topics.{AgentsTopic, ClusterTopic}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.commons._
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.{PipelineBlueprintCollector, PipelineInstanceCollector}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.AgentStatsManager.{GetAvailableAgentsRequest, GetAvailableAgentsResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager._
import io.logbee.keyscore.frontier.cluster.pipeline.subordinates.PipelineDeployer
import io.logbee.keyscore.frontier.cluster.pipeline.subordinates.PipelineDeployer.CreatePipelineRequest
import io.logbee.keyscore.model.blueprint._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ClusterPipelineManager {

  case object RequestExistingPipelines

  case class RequestExistingBlueprints()

  case class CreatePipeline(blueprintRef: BlueprintRef)

  case class DeletePipeline(id: UUID)

  case object DeleteAllPipelines

  case object InitializeMessage

  def apply(clusterAgentManager: ActorRef): Props = {
    Props(new ClusterPipelineManager(
      clusterAgentManager,
      (ref, context) => context.actorSelection(ref.path / LocalPipelineManagerPath)
    ))
  }

  def apply(clusterAgentManager: ActorRef, localPipelineManager: (ActorRef, ActorContext) => ActorSelection): Props = {
    Props(new ClusterPipelineManager(clusterAgentManager, localPipelineManager))
  }
}

/**
  * The ClusterPipelineManager<br>
  * - starts a new PipelineDeployer to deploy a new Pipeline from a BlueprintRef <br>
  * - deletes a specific or all pipelines <br>
  * - forwards all Controller messages
  * - creates Blueprint- and ConfigurationCollectors and send them to all agents.
  *
  * @param clusterAgentManager
  * @param localPipelineManagerResolution
  */
class ClusterPipelineManager(clusterAgentManager: ActorRef, localPipelineManagerResolution: (ActorRef, ActorContext) => ActorSelection) extends Actor with ActorLogging {

  implicit val timeout = Timeout(15 seconds)
  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  var agentStatsManager: ActorRef = _
  var agentCapabilitiesManager: ActorRef = _

  override def preStart(): Unit = {
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Subscribe(ClusterTopic, self)
    mediator ! Publish(ClusterTopic, ActorJoin("ClusterPipelineManager", self))
    log.info("ClusterPipelineManager started.")
    self ! InitializeMessage
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, ActorLeave("ClusterPipelineManager", self))
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Unsubscribe(ClusterTopic, self)
    log.info("ClusterPipelineManager stopped.")
  }

  case class CreateClusterPipelineManagerState(agentStatsManager: ActorRef = null, agentCapabilitiesManager: ActorRef = null) {
    def isComplete: Boolean = agentStatsManager != null && agentCapabilitiesManager != null
  }

  override def receive: Receive = {
    case InitializeMessage =>
      log.debug("WhoIs AgentStats")
      mediator ! Publish(Topics.WhoIsTopic, WhoIs(AgentStatsService))
      log.debug("WhoIs AgentCapabilities")
      mediator ! Publish(Topics.WhoIsTopic, WhoIs(AgentCapabilitiesService))

      context.become(initializing(CreateClusterPipelineManagerState()))
  }

  private def initializing(state: CreateClusterPipelineManagerState): Receive = {
    case HereIam(AgentStatsService, ref) =>
      log.debug("Received AgentStatsService")
      agentStatsManager = ref
      maybeRunning(state.copy(agentStatsManager = ref))
    case HereIam(AgentCapabilitiesService, ref) =>
      log.debug("Received AgentCapabilitiesService")
      agentCapabilitiesManager = ref
      maybeRunning(state.copy(agentCapabilitiesManager = ref))

  }

  private def running: Receive = {
    case CreatePipeline(blueprintRef) =>
      val pipelineDeployer = context.actorOf(PipelineDeployer(localPipelineManagerResolution))
      pipelineDeployer forward CreatePipelineRequest(blueprintRef)

    case DeletePipeline(id) =>
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            context.actorSelection(agent.path / PipelineSchedulerPath) ! DeletePipelineOrder(id)
          })
        case Failure(e) => log.warning(s"Failed to delete Pipeline with id ($id): $e")
      }

    case DeleteAllPipelines =>
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            context.actorSelection(agent.path / PipelineSchedulerPath) ! DeleteAllPipelinesOrder
          })
        case Failure(e) => log.warning(s"Failed to delete all pipelines: $e")
      }

    case message: PauseFilter =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) tell (message, _sender)
          })
        case Failure(e) => log.warning(s"Failed to forward message [$message]: $e")
      }

    case message: DrainFilterValve =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) tell (message, _sender)
          })
        case Failure(e) => log.warning(s"Failed to forward message [$message]: $e")
      }

    case message: InsertDatasets =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) tell (message, _sender)
          })
        case Failure(e) => log.warning(s"Failed to forward message [$message]: $e")
      }

    case message: ExtractDatasets =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) tell (message, _sender)
          })
        case Failure(e) => log.warning(s"Failed to forward message [$message]: $e")
      }

    case message: ConfigureFilter =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) tell (message, _sender)
          })
        case Failure(e) => log.warning(s"Failed to forward message [$message]: $e")
      }

    case message: CheckFilterState =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) tell (message, _sender)
          })
        case Failure(e) => log.warning(s"Failed to forward message [$message]: $e")
      }

    case message: ClearBuffer =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) tell (message, _sender)
          })
        case Failure(e) => log.warning(s"Failed to forward message [$message]: $e")
      }

    case RequestExistingPipelines =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          context.system.actorOf(PipelineInstanceCollector(_sender, agents, localPipelineManagerResolution)(5 seconds))
        case Failure(e) => log.warning(s"Failed to get existing pipelines: $e")
      }

    case RequestExistingBlueprints() =>
      //TODO Let the PBCollector use the sender ref
      val _sender = sender
      val future: Future[List[ActorRef]] = ask(agentStatsManager, GetAvailableAgentsRequest).mapTo[List[ActorRef]]
      future.onComplete {
        case Success(agents) =>
          log.info(s"Success: $agents")
          val collector = context.system.actorOf(PipelineBlueprintCollector(sender, agents))
          agents.foreach(agent => {
            localPipelineManagerResolution(agent, context) ! RequestPipelineBlueprints(collector)
          })
        case Failure(e) => log.warning(s"Failed to get existing blueprints: $e")
      }
  }

  private def maybeRunning(state: CreateClusterPipelineManagerState): Unit = {
    if (state.isComplete) {
      context.become(running)
      log.info("became running.")
    }
    else {
      context.become(initializing(state))
    }
  }
}
