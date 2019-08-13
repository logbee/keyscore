package io.logbee.keyscore.agent.pipeline

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Behaviors.receiveMessage
import akka.actor.{Actor, ActorLogging, ActorRef, Props, typed}
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.runtimes.StageLogicProvider
import io.logbee.keyscore.agent.runtimes.StageLogicProvider.{LoadSuccess, StageLogicProviderRequest, StageLogicProviderResponse}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.conversion.UUIDConversion._
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.model.pipeline.StageSupervisor
import io.logbee.keyscore.pipeline.api._
import io.logbee.keyscore.pipeline.api.stage._

import scala.collection.mutable

object FilterManager {

  def apply(provider: List[typed.ActorRef[StageLogicProviderRequest]] = List.empty): Props = Props(new FilterManager(provider))

  case object RequestDescriptors

  case class DescriptorsResponse(descriptors: List[Descriptor])

  trait StageCreated

  case class CreateSourceStage(blueprintRef: BlueprintRef, supervisor: StageSupervisor, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class SourceStageCreated(blueprintRef: BlueprintRef, stage: SourceStage) extends StageCreated

  case class CreateSinkStage(blueprintRef: BlueprintRef, supervisor: StageSupervisor, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class SinkStageCreated(blueprintRef: BlueprintRef, stage: SinkStage) extends StageCreated

  case class CreateFilterStage(blueprintRef: BlueprintRef, supervisor: StageSupervisor, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class FilterStageCreated(blueprintRef: BlueprintRef, stage: FilterStage) extends StageCreated

  case class CreateBranchStage(blueprintRef: BlueprintRef, supervisor: StageSupervisor, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class BranchStageCreated(blueprintRef: BlueprintRef, stage: BranchStage) extends StageCreated

  case class CreateMergeStage(blueprintRef: BlueprintRef, supervisor: StageSupervisor, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class MergeStageCreated(blueprintRef: BlueprintRef, stage: MergeStage) extends StageCreated

  case class DescriptorNotFound(descriptorRef: DescriptorRef, blueprintRef: BlueprintRef)

  case class StageCreationFailed(descriptorRef: DescriptorRef, blueprintRef: BlueprintRef)

  case class Registration(descriptor: Descriptor, provider: typed.ActorRef[StageLogicProviderRequest])
}

/**
  * The '''FilterManager''' manages all `Extensions` of the corresponding `Agent` and creates all `Stages` for a `Pipeline`.
  *
  * @todo Renaming?
  */
class FilterManager(providers: List[typed.ActorRef[StageLogicProviderRequest]]) extends Actor with ActorLogging {

  import akka.actor.typed.scaladsl.adapter._

  private val descriptors = mutable.HashMap.empty[DescriptorRef, Registration]

  override def preStart(): Unit = {
    providers.foreach(provider => provider ! StageLogicProvider.Load(self))
    log.info(" started.")
  }

  override def postStop(): Unit = {
    log.info(" stopped.")
  }

  override def receive: Receive = {

    case LoadSuccess(descriptors, provider) =>
      descriptors.foreach(descriptor => {
        this.descriptors.put(descriptor.ref, Registration(descriptor, provider))
        log.debug("Loaded descriptor <{}> provided by: {}", descriptor.ref.uuid, provider)
      })

    case RequestDescriptors =>
      log.debug("Sending Descriptors.")
      sender ! DescriptorsResponse(descriptors.values.map(_.descriptor).toList)

    case CreateSinkStage(blueprintRef, supervisor, stageContext, descriptorRef, configuration) =>

      log.debug(s"Creating SinkStage: <{}>", blueprintRef.uuid)

      descriptors.get(descriptorRef) match {

        case Some(Registration(descriptor, provider)) =>

          val replyTo = sender()

          context.spawnAnonymous(Behaviors.setup[StageLogicProviderResponse] { context =>
            provider ! StageLogicProvider.CreateSinkStage(descriptorRef, LogicParameters(blueprintRef, supervisor, stageContext, configuration), context.self)
            logicProviderAdapter(descriptorRef, blueprintRef, replyTo)
          })

        case _ =>
          log.warning(s"Descriptor <{}> for sinkStage <{}> not found.", blueprintRef.uuid, descriptorRef.uuid)
          sender ! DescriptorNotFound(descriptorRef, blueprintRef)
      }

    case CreateSourceStage(blueprintRef, supervisor, stageContext, descriptorRef, configuration) =>

      log.debug(s"Creating SourceStage: ${descriptorRef.uuid}")

      descriptors.get(descriptorRef) match {

        case Some(Registration(descriptor, provider)) =>

          val replyTo = sender()

          context.spawnAnonymous(Behaviors.setup[StageLogicProviderResponse] { context =>
            provider ! StageLogicProvider.CreateSourceStage(descriptorRef, LogicParameters(blueprintRef, supervisor, stageContext, configuration), context.self)
            logicProviderAdapter(descriptorRef, blueprintRef, replyTo)
          })

        case _ =>
          log.warning(s"Descriptor <{}> for sourceStage <{}> not found.", blueprintRef.uuid, descriptorRef.uuid)
          sender ! DescriptorNotFound(descriptorRef, blueprintRef)
      }

    case CreateFilterStage(blueprintRef, supervisor, stageContext, descriptorRef, configuration) =>

      log.debug(s"Creating FilterStage: ${descriptorRef.uuid}")

      descriptors.get(descriptorRef) match {

        case Some(Registration(descriptor, provider)) =>

          val replyTo = sender()

          context.spawnAnonymous(Behaviors.setup[StageLogicProviderResponse] { context =>
            provider ! StageLogicProvider.CreateFilterStage(descriptorRef, LogicParameters(blueprintRef, supervisor, stageContext, configuration), context.self)
            logicProviderAdapter(descriptorRef, blueprintRef, replyTo)
          })

        case _ =>
          log.warning(s"Descriptor <{}> for filterStage <{}> not found.", blueprintRef.uuid, descriptorRef.uuid)
          sender ! DescriptorNotFound(descriptorRef, blueprintRef)
      }

    case CreateBranchStage(blueprintRef, supervisor, stageContext, descriptorRef, configuration) =>

      log.debug(s"Creating BranchStage: ${descriptorRef.uuid}")

      descriptors.get(descriptorRef) match {

        case Some(Registration(descriptor, provider)) =>

          val replyTo = sender()

          context.spawnAnonymous(Behaviors.setup[StageLogicProviderResponse] { context =>
            provider ! StageLogicProvider.CreateBranchStage(descriptorRef, LogicParameters(blueprintRef, supervisor, stageContext, configuration), context.self)
            logicProviderAdapter(descriptorRef, blueprintRef, replyTo)
          })

        case _ =>
          log.warning(s"Descriptor <{}> for stage <{}> not found.", blueprintRef.uuid, descriptorRef.uuid)
          sender ! DescriptorNotFound(descriptorRef, blueprintRef)
      }

    case CreateMergeStage(blueprintRef, supervisor, stageContext, descriptorRef, configuration) =>

      log.debug(s"Creating MergeStage: ${descriptorRef.uuid}")

      descriptors.get(descriptorRef) match {

        case Some(Registration(descriptor, provider)) =>

          val replyTo = sender()

          context.spawnAnonymous(Behaviors.setup[StageLogicProviderResponse] { context =>
            provider ! StageLogicProvider.CreateMergeStage(descriptorRef, LogicParameters(blueprintRef, supervisor, stageContext, configuration), context.self)
            logicProviderAdapter(descriptorRef, blueprintRef, replyTo)
          })

        case _ =>
          log.warning(s"Descriptor <{}> for stage <{}> not found.", blueprintRef.uuid, descriptorRef.uuid)
          sender ! DescriptorNotFound(descriptorRef, blueprintRef)
      }

    case Ready =>
      sender ! Ready
  }

  private def logicProviderAdapter(descriptorRef: DescriptorRef, blueprintRef: BlueprintRef, replyTo: ActorRef) = {

    receiveMessage[StageLogicProviderResponse] {

      case StageLogicProvider.SinkStageCreated(`descriptorRef`, stage, _) =>
        replyTo tell(SinkStageCreated(blueprintRef, stage), self)
        log.debug("Created SinkStage: <{}>", blueprintRef.uuid)
        Behaviors.stopped

      case StageLogicProvider.SourceStageCreated(`descriptorRef`, stage, _) =>
        replyTo tell(SourceStageCreated(blueprintRef, stage), self)
        log.debug("Created SourceStage: <{}>", blueprintRef.uuid)
        Behaviors.stopped

      case StageLogicProvider.FilterStageCreated(`descriptorRef`, stage, _) =>
        replyTo tell(FilterStageCreated(blueprintRef, stage), self)
        log.debug("Created FilterStage: <{}>", blueprintRef.uuid)
        Behaviors.stopped

      case StageLogicProvider.BranchStageCreated(`descriptorRef`, stage, _) =>
        replyTo tell(BranchStageCreated(blueprintRef, stage), self)
        log.debug("Created BranchStage: <{}>", blueprintRef.uuid)
        Behaviors.stopped

      case StageLogicProvider.MergeStageCreated(`descriptorRef`, stage, _) =>
        replyTo tell(MergeStageCreated(blueprintRef, stage), self)
        log.debug("Created MergeStage: <{}>", blueprintRef.uuid)
        Behaviors.stopped

      case StageLogicProvider.DescriptorNotFound(`descriptorRef`, `blueprintRef`, _) =>
        log.error("Failed to create stage: <{}>", blueprintRef.uuid)
        replyTo tell(DescriptorNotFound(descriptorRef, blueprintRef), self)
        Behaviors.stopped

      case StageLogicProvider.StageCreationFailed(`descriptorRef`, `blueprintRef`, _) =>
        log.error("Failed to create stage: <{}>", blueprintRef.uuid)
        replyTo tell(StageCreationFailed(descriptorRef, blueprintRef), self)
        Behaviors.stopped
    }
  }
}
