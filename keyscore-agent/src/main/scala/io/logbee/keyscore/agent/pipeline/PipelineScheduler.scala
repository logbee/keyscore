package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.PipelineScheduler._
import io.logbee.keyscore.commons.cluster.{CreatePipelineOrder, DeleteAllPipelinesOrder, DeletePipelineOrder}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.model.PipelineConfiguration

import scala.concurrent.duration._
import scala.language.postfixOps

object PipelineScheduler {

  private val SUPERVISOR_NAME_PREFIX = "pipeline:"

  case class CreateNewPipeline(configuration: PipelineConfiguration)

  case class UpdatePipeline(configuration: PipelineConfiguration)

  private case class SupervisorTerminated(supervisor: ActorRef, configuration: PipelineConfiguration)

  def apply(filterManager: ActorRef): Props = Props(new PipelineScheduler(filterManager))
}

class PipelineScheduler(filterManager: ActorRef) extends Actor with ActorLogging {

  import context._
  implicit val timeout: Timeout = 10 seconds

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def postStop(): Unit = {
    log.info("Stopped")
  }

  override def receive: Receive = {

    case CreatePipelineOrder(configuration) =>
      child(nameFrom(configuration)) match {
        case Some(_) => self ! UpdatePipeline(configuration)
        case None => self ! CreateNewPipeline(configuration)
      }
    case CreateNewPipeline(configuration) =>
      log.info("Received Create Pipeline: " + configuration.id)
      val supervisor = actorOf(PipelineSupervisor(filterManager), nameFrom(configuration))
      log.info("Send CreatePipelineMessage to" + supervisor.toString())
      supervisor ! PipelineSupervisor.CreatePipeline(configuration)
      watchWith(supervisor, SupervisorTerminated(supervisor, configuration))

    case UpdatePipeline(configuration) =>
      log.info("Received Update Pipeline: " + configuration.id)
      child(nameFrom(configuration)).foreach(child => {
        log.info(s"Stopping PipelineSupervisor for pipeline: ${configuration.id}")
        unwatch(child)
        watchWith(child, CreateNewPipeline(configuration))
        context.stop(child)
      })

    case DeletePipelineOrder(id) =>
      child(nameFrom(id)).foreach(child => context.stop(child))

    case DeleteAllPipelinesOrder =>
      children.foreach(child => context.stop(child))

    case message: RequestPipelineInstance =>
      children.foreach( supervisor => {
        supervisor forward message
      })

    case message: RequestPipelineConfigurations =>
      children.foreach( supervisor => {
        supervisor forward message
      })

    case SupervisorTerminated(supervisor, configuration) =>
      log.info(s"PipelineSupervisor terminated: $configuration")

    case message: PauseFilter =>
      children.foreach( supervisor => {
        supervisor forward  message
      })

    case message: DrainFilterValve =>
      children.foreach(supervisor => {
        supervisor forward message
      })

    case message: InsertDatasets =>
      children.foreach( supervisor => {
        supervisor forward message
      })

    case message: ExtractDatasets =>
      children.foreach( supervisor =>  {
        supervisor forward message
      })

    case message: ConfigureFilter =>
      children.foreach( supervisor => {
        supervisor forward message
      })

    case _ => log.info("Failure")
  }

  def nameFrom(configuration: PipelineConfiguration): String = {
    nameFrom(configuration.id)
  }

  def nameFrom(id: UUID): String = {
    s"$SUPERVISOR_NAME_PREFIX$id"
  }

  def isSupervisor(ref: ActorRef): Boolean = {
    ref.path.name.startsWith(SUPERVISOR_NAME_PREFIX)
  }
}
