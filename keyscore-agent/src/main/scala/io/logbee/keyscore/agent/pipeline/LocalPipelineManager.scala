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
  * The LocalPipelineManager does:
  *
  * - manage all the local pipelines of an agent
  *
  * - create, delete, update and forwarding of ControllerMessages
  *
  * @param filterManager ActorRef of filterManager
  */
class LocalPipelineManager(filterManager: ActorRef) extends Actor with ActorLogging {


  import context._
  implicit val timeout: Timeout = 10 seconds
  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(Topics.WhoIsTopic, self)
    log.info("StartUp complete.")
  }

  override def postStop(): Unit = {
    log.info("Stopped")
  }

  override def receive: Receive = {
    case WhoIs(LocalPipelineService) =>
      sender ! HereIam(LocalPipelineService, self)

    case CreatePipelineOrder(blueprint) =>
      log.info(s"Received Pipeline Creation Order for: $blueprint")
      child(nameFrom(blueprint)) match {
        case Some(_) => self ! UpdatePipeline(blueprint)
        case None => self ! CreateNewPipeline(blueprint)
      }
    case CreateNewPipeline(blueprint) =>
      log.info("Received Create Pipeline: " + blueprint.ref.uuid)
      val supervisor = actorOf(PipelineSupervisor(filterManager), nameFrom(blueprint))
      log.info("Send CreatePipelineMessage to" + supervisor.toString())
      supervisor ! PipelineSupervisor.CreatePipeline(blueprint)
      watchWith(supervisor, SupervisorTerminated(supervisor, blueprint))

    case UpdatePipeline(blueprint) =>
      log.info("Received Update Pipeline: " + blueprint.ref.uuid)
      child(nameFrom(blueprint)).foreach(child => {
        log.info(s"Stopping PipelineSupervisor for pipeline: ${blueprint.ref.uuid}")
        unwatch(child)
        watchWith(child, CreateNewPipeline(blueprint))
        context.stop(child)
      })

    case DeletePipelineOrder(id) =>
      child(nameFrom(id)).foreach(child => context.stop(child))

    case DeleteAllPipelinesOrder =>
      children.foreach(child => context.stop(child))

    case message: RequestPipelineInstance =>
      log.debug(s"Sender of ReqInstance is: ${sender()}")
      children.foreach( supervisor => {
        supervisor forward message
      })

    case message: RequestPipelineBlueprints =>
      children.foreach( supervisor => {
        supervisor forward message
      })

    case SupervisorTerminated(supervisor, configuration) =>
      log.info(s"PipelineSupervisor terminated: $configuration")

    case message: PauseFilter =>
      log.info(s"Received PauseFilter with ${message} from $sender")
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
    case e => log.info(s"Unknown message received: $e")
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
