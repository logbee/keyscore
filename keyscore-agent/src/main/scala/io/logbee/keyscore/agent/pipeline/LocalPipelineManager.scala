package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.util.Timeout
import io.logbee.keyscore.agent.pipeline.LocalPipelineManager._
import io.logbee.keyscore.commons.{HereIam, LocalPipelineService, WhoIs}
import io.logbee.keyscore.commons.cluster.{CreatePipelineOrder, DeleteAllPipelinesOrder, DeletePipelineOrder, Topics}
import io.logbee.keyscore.commons.pipeline._
import io.logbee.keyscore.model.blueprint.PipelineBlueprint

import scala.concurrent.duration._
import scala.language.postfixOps


object LocalPipelineManager {

  private val SUPERVISOR_NAME_PREFIX = "pipeline:"

  case class CreateNewPipeline(blueprint: PipelineBlueprint)

  case class UpdatePipeline(blueprint: PipelineBlueprint)

  private case class SupervisorTerminated(supervisor: ActorRef, blueprint: PipelineBlueprint)

  def apply(filterManager: ActorRef): Props = Props(new LocalPipelineManager(filterManager))
}

/**
  * The '''LocalPipelineManager''' manages the requests for all Pipelines on his Agent. <br><br>
  * For creating a new Pipeline he creates a [[io.logbee.keyscore.agent.pipeline.PipelineSupervisor]] <br>
  * He can also delete a single or all Pipelines. <br><br>
  * The LocalPipelineManager also forwards all `Controller` requests to the corresponding `Supervisor`.
  *
  * @param filterManager [[io.logbee.keyscore.agent.pipeline.FilterManager]]
  */
class LocalPipelineManager(filterManager: ActorRef) extends Actor with ActorLogging {

  import context._
  implicit val timeout: Timeout = 10 seconds

  override def preStart(): Unit = {
    log.info(s" started.")
  }

  override def postStop(): Unit = {
    log.info(s" stopped.")
  }

  override def receive: Receive = {
    case CreatePipelineOrder(blueprint) =>
      log.info(s"Received Order to create a Pipeline for: <$blueprint>")
      child(nameFrom(blueprint)) match {
        case Some(_) => self ! UpdatePipeline(blueprint)
        case None => self ! CreateNewPipeline(blueprint)
      }
    case CreateNewPipeline(blueprint) =>
      log.info(s"Creating Pipeline: <${blueprint.ref.uuid}>")
      val supervisor = actorOf(PipelineSupervisor(filterManager), nameFrom(blueprint))
      log.debug(s"Send CreatePipelineMessage to ${supervisor.toString()}")
      supervisor ! PipelineSupervisor.CreatePipeline(blueprint)
      watchWith(supervisor, SupervisorTerminated(supervisor, blueprint))

    case UpdatePipeline(blueprint) =>
      log.debug(s"Received Update Pipeline: <${blueprint.ref.uuid}>")
      child(nameFrom(blueprint)).foreach(child => {
        log.debug(s"Stopping child for pipeline: <${blueprint.ref.uuid}>")
        unwatch(child)
        watchWith(child, CreateNewPipeline(blueprint))
        context.stop(child)
      })

    case DeletePipelineOrder(id) =>
      log.debug(s"Stopping child for pipeline <$id>")
      child(nameFrom(id)).foreach(child => context.stop(child))

    case DeleteAllPipelinesOrder =>
      log.warning("Received Order to delete all Pipelines. Stopping all children.")
      children.foreach(child => context.stop(child))

    case RequestPipelineInstance =>
      children.foreach( supervisor => {
        supervisor forward RequestPipelineInstance
      })

    case message: RequestPipelineBlueprints =>
      children.foreach( supervisor => {
        supervisor forward message
      })

    case SupervisorTerminated(supervisor, configuration) =>
      log.warning(s"PipelineSupervisor <$supervisor> has terminated: $configuration")

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

    case message: CheckFilterState =>
      children.foreach( supervisor => {
        supervisor forward message
      })

    case message: ClearBuffer =>
      children.foreach( supervisor => {
        supervisor forward message
      })
  }

  def nameFrom(blueprint: PipelineBlueprint): String = {
    nameFrom(blueprint.ref)
  }

  def nameFrom(id: UUID): String = {
    s"$SUPERVISOR_NAME_PREFIX$id"
  }

  def isSupervisor(ref: ActorRef): Boolean = {
    ref.path.name.startsWith(SUPERVISOR_NAME_PREFIX)
  }
}
