package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.PipelineManager._
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor.{ConfigurePipeline, CreatePipeline}
import io.logbee.keyscore.model.PipelineConfiguration

import scala.concurrent.duration._
import scala.language.postfixOps

object PipelineManager {

  private val SUPERVISOR_NAME_PREFIX = "pipeline:"

  case class CreatePipeline(configuration: PipelineConfiguration)

  case class UpdatePipeline(configuration: PipelineConfiguration)

  case class DeletePipeline(configuration: PipelineConfiguration)

  case object RequestPipelineState

  private case class SupervisorTerminated(supervisor: ActorRef, configuration: PipelineConfiguration)

  def apply(filterManager: ActorRef): Props = Props(new PipelineManager(filterManager))
}

class PipelineManager(filterManager: ActorRef) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 10 seconds

  import context._

  override def preStart(): Unit = {
    log.info("[Agent / Pipelinemanager]: StartUp complete.")
  }

  override def postStop(): Unit = {
    log.info("[Agent / Pipelinemanager]:Stopped")
  }

  override def receive: Receive = {

    case PipelineManager.CreatePipeline(configuration) =>
      log.info("[Agent / Pipelinemanager]: Received Create Pipeline")
      val supervisor = actorOf(PipelineSupervisor(filterManager), nameFromConfiguration(configuration))
      log.info("[Agent / PipelineManager]: send CreatePipelineMessage to" + supervisor.toString())
      supervisor ! PipelineSupervisor.CreatePipeline(configuration)
      watchWith(supervisor, SupervisorTerminated(supervisor, configuration))

    case UpdatePipeline(configuration) =>
      child(nameFromConfiguration(configuration)).foreach(child => child ! ConfigurePipeline(configuration))

    case DeletePipeline(configuration) =>
      child(nameFromConfiguration(configuration)).foreach(child => context.stop(child))

    case RequestPipelineState =>
      actorOf(PipelineStateAggregator(sender, children.filter(isSupervisor)))

    case SupervisorTerminated(supervisor, configuration) =>
      log.info(s"PipelineSupervisor terminated: $configuration")

    case _ => log.info("[Agent / Pipelinemanager]: Failure")
  }

  def nameFromConfiguration(configuration: PipelineConfiguration): String = {
    s"$SUPERVISOR_NAME_PREFIX${configuration.id}"
  }

  def isSupervisor(ref: ActorRef): Boolean = {
    ref.path.name.startsWith(SUPERVISOR_NAME_PREFIX)
  }
}
