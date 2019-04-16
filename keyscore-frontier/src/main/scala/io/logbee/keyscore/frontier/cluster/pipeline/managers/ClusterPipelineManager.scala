package io.logbee.keyscore.frontier.cluster.pipeline.managers

import java.util.UUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSelection, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.pattern.ask
import akka.util.Timeout
import io.logbee.keyscore.commons._
import io.logbee.keyscore.commons.cluster.Paths.{LocalPipelineManagerPath, PipelineSchedulerPath}
import io.logbee.keyscore.commons.cluster.Topics.{AgentsTopic, ClusterTopic}
import io.logbee.keyscore.commons.cluster._
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.commons.util.ServiceDiscovery.discover
import io.logbee.keyscore.frontier.cluster.pipeline.collectors.{PipelineBlueprintCollector, PipelineInstanceCollector}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.AgentStatsManager.{GetAvailableAgentsRequest, GetAvailableAgentsResponse}
import io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterPipelineManager._
import io.logbee.keyscore.frontier.cluster.pipeline.subordinates.PipelineDeployer
import io.logbee.keyscore.frontier.cluster.pipeline.subordinates.PipelineDeployer.CreatePipelineRequest
import io.logbee.keyscore.model.blueprint._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}


object ClusterPipelineManager {

  case object RequestExistingPipelines

  case class RequestExistingBlueprints()

  case class CreatePipeline(blueprintRef: BlueprintRef)

  case class DeletePipeline(id: UUID)

  case object DeleteAllPipelines

  case object InitCPM

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
  * The '''ClusterPipelineManager''' <br>
  * - starts a new `PipelineDeployer` to deploy a new Pipeline from a BlueprintRef <br>
  * - deletes a specific or all pipelines <br>
  * - forwards all `Controller` messages <br>
  * - creates Blueprint- and Configuration `Collectors` and send them to all agents.
  *
  * @todo Error Handling
  *
  * @param clusterAgentManager The [[io.logbee.keyscore.frontier.cluster.pipeline.managers.ClusterAgentManager]]
  * @param localPipelineManagerResolution ~anonymous
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
    mediator ! Publish(ClusterTopic, ActorJoin(Roles.ClusterPipelineManager, self))
    log.info(" started.")
    self ! InitCPM
  }

  override def postStop(): Unit = {
    mediator ! Publish(ClusterTopic, ActorLeave(Roles.ClusterPipelineManager, self))
    mediator ! Subscribe(AgentsTopic, self)
    mediator ! Unsubscribe(ClusterTopic, self)
    log.info(" stopped.")
  }

  override def receive: Receive = {
    case InitCPM =>
      discover(Seq(AgentStatsService, AgentCapabilitiesService)).onComplete {
        case Success(services) =>
          agentStatsManager = services(AgentStatsService)
          agentCapabilitiesManager = services(AgentCapabilitiesService)
          context.become(running)
        case Failure(exception) =>
          log.error(exception, "Couldn't retrieve services.")
        // TODO: Handle discover errors!
      }
  }

  private def running: Receive = {
    case CreatePipeline(blueprintRef) =>
      log.debug(s"Started a PipelineDeployer for <${blueprintRef.uuid}>")
      val pipelineDeployer = context.actorOf(PipelineDeployer(localPipelineManagerResolution))
      pipelineDeployer forward CreatePipelineRequest(blueprintRef)

    case DeletePipeline(id) =>
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          agents.foreach(agent => {
            log.debug(s"Forwarding DeletePipeline($id) to $agent")
            context.actorSelection(agent.path / PipelineSchedulerPath) ! DeletePipelineOrder(id)
          })
        case Failure(e) =>
          log.error(e, message = s"Failed to delete Pipeline with id <$id>: $e")
        case _ =>
          log.error(s"Failed to query the available agents to delete pipeline with id <$id>!")
      }

    case DeleteAllPipelines => forwardToLocalPipelineManagerOfAvailableAgents(sender, DeleteAllPipelinesOrder)

    case message: PauseFilter => forwardToLocalPipelineManagerOfAvailableAgents(sender, message)

    case message: DrainFilterValve => forwardToLocalPipelineManagerOfAvailableAgents(sender, message)

    case message: InsertDatasets => forwardToLocalPipelineManagerOfAvailableAgents(sender, message)

    case message: ExtractDatasets => forwardToLocalPipelineManagerOfAvailableAgents(sender, message)

    case message: ConfigureFilter => forwardToLocalPipelineManagerOfAvailableAgents(sender, message)

    case message: CheckFilterState => forwardToLocalPipelineManagerOfAvailableAgents(sender, message)

    case message: ClearBuffer => forwardToLocalPipelineManagerOfAvailableAgents(sender, message)

    case RequestExistingPipelines =>
      val _sender = sender
      (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
        case Success(GetAvailableAgentsResponse(agents)) =>
          context.system.actorOf(PipelineInstanceCollector(_sender, agents, localPipelineManagerResolution)(5 seconds))
        case Failure(e) =>
          log.error(e, message = s"Failed to get existing pipelines: $e")
        case _ =>
          log.error("Failed to query the available agents!")
      }

    case RequestExistingBlueprints() =>
      val future: Future[List[ActorRef]] = ask(agentStatsManager, GetAvailableAgentsRequest).mapTo[List[ActorRef]]
      future.onComplete {
        case Success(agents) =>
          val collector = context.system.actorOf(PipelineBlueprintCollector(sender, agents))
          agents.foreach(agent => {
            log.debug(s"Collecting PipelineBlueprints at $agent")
            localPipelineManagerResolution(agent, context) ! RequestPipelineBlueprints(collector)
          })
        case Failure(e) =>
          log.error(e, message = s"Failed to request existing blueprints: $e")
      }
  }

  private def forwardToLocalPipelineManagerOfAvailableAgents(sender: ActorRef, message: Any): Unit = {
    (agentStatsManager ? GetAvailableAgentsRequest).onComplete {
      case Success(GetAvailableAgentsResponse(agents)) =>
        agents.foreach(agent => {
          log.debug(s"Forwarded message $message to $agent")
          localPipelineManagerResolution(agent, context) tell(message, sender)
        })
      case Failure(e) =>
        log.error(e, s"Failed to forward message [${message.getClass.getSimpleName}]")
      case _ =>
        log.error(s"Failed to query the available agents to forward message [${message.getClass.getSimpleName}]!")
    }
  }
}
