package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.PipelineManager._
import io.logbee.keyscore.model.PipelineConfiguration

import scala.concurrent.duration._
import scala.language.postfixOps

object PipelineManager {

  private val SUPERVISOR_NAME_PREFIX = "pipeline:"

  case class CreatePipeline(configuration: PipelineConfiguration)

  case class CreateNewPipeline(configuration: PipelineConfiguration)

  case class UpdatePipeline(configuration: PipelineConfiguration)

  case class DeletePipeline(configuration: PipelineConfiguration)

  case object RequestPipelineState

  private case class SupervisorTerminated(supervisor: ActorRef, configuration: PipelineConfiguration)

  def apply(filterManager: ActorRef): Props = Props(new PipelineManager(filterManager))
}

class PipelineManager(filterManager: ActorRef) extends Actor with ActorLogging {

  import context._
  implicit val timeout: Timeout = 10 seconds


  override def preStart(): Unit = {
    log.info("[Agent | PipelineManager]: StartUp complete.")
  }

  override def postStop(): Unit = {
    log.info("[Agent | PipelineManager]: Stopped")
  }

  override def receive: Receive = {

    case PipelineManager.CreatePipeline(configuration) =>
      child(nameFromConfiguration(configuration)) match {
        case Some(_) => self ! UpdatePipeline(configuration)
        case None => self ! CreateNewPipeline(configuration)
      }
    case CreateNewPipeline(configuration) =>
      log.info("[Agent | PipelineManager]: Received Create Pipeline: " + configuration.id)
      val supervisor = actorOf(PipelineSupervisor(filterManager), nameFromConfiguration(configuration))
      log.info("[Agent | PipelineManager]: send CreatePipelineMessage to" + supervisor.toString())
      supervisor ! PipelineSupervisor.CreatePipeline(configuration)
      watchWith(supervisor, SupervisorTerminated(supervisor, configuration))

    case UpdatePipeline(configuration) =>
      log.info("[Agent | PipelineManager]: Received Update Pipeline: " + configuration.id)
      child(nameFromConfiguration(configuration)).foreach(child => {
        log.info(s"Stopping PipelineSupervisor for pipeline: ${configuration.id}")
        unwatch(child)
        watchWith(child, CreateNewPipeline(configuration))
        context.stop(child)
        log.info(s"Stopped PipelineSupervisor for pipeline: ${configuration.id}")
      })

    case DeletePipeline(configuration) =>
      log.info("[Agent | PipelineManager]: Delete Create Pipeline: " + configuration.id)
      child(nameFromConfiguration(configuration)).foreach(child => context.stop(child))

    case RequestPipelineState =>
      actorOf(PipelineStateAggregator(sender, children.filter(isSupervisor)))

    case SupervisorTerminated(supervisor, configuration) =>
      log.info(s"PipelineSupervisor terminated: $configuration")

    case _ => log.info("[Agent | PipelineManager]: Failure")
  }

  def nameFromConfiguration(configuration: PipelineConfiguration): String = {
    s"$SUPERVISOR_NAME_PREFIX${configuration.id}"
  }

  def isSupervisor(ref: ActorRef): Boolean = {
    ref.path.name.startsWith(SUPERVISOR_NAME_PREFIX)
  }
}
