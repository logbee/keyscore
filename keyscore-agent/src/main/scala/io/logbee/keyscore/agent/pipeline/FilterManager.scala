package io.logbee.keyscore.agent.pipeline

import akka.actor
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.{FilterExtension, SinkExtension, SourceExtension}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.blueprint.BlueprintRef
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.conversion.UUIDConversion._
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.pipeline.api.LogicProviderFactory._
import io.logbee.keyscore.pipeline.api._
import io.logbee.keyscore.pipeline.api.stage._

import scala.collection.mutable

object FilterManager {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new FilterManager)

  case object RequestDescriptors

  case class DescriptorsResponse(descriptors: List[Descriptor])

  trait StageCreated

  case class CreateSourceStage(blueprintRef: BlueprintRef, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class SourceStageCreated(blueprintRef: BlueprintRef, stage: SourceStage) extends StageCreated

  case class CreateSinkStage(blueprintRef: BlueprintRef, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class SinkStageCreated(blueprintRef: BlueprintRef, stage: SinkStage) extends StageCreated

  case class CreateFilterStage(blueprintRef: BlueprintRef, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class FilterStageCreated(blueprintRef: BlueprintRef, stage: FilterStage) extends StageCreated

  case class CreateBranchStage(blueprintRef: BlueprintRef, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class BranchStageCreated(blueprintRef: BlueprintRef, stage: BranchStage) extends StageCreated

  case class CreateMergeStage(blueprintRef: BlueprintRef, context: StageContext, descriptor: DescriptorRef, configuration: Configuration)

  case class MergeStageCreated(blueprintRef: BlueprintRef, stage: MergeStage) extends StageCreated

}

/**
  * The '''FilterManager''' manages all `Extensions` of the corresponding `Agent` and creates all `Stages` for a `Pipeline`.
  *
  * @todo Renaming?
  */
class FilterManager extends Actor with ActorLogging {

  private val eventBus = context.system.eventStream
  private val filterLoader = new FilterLoader

  private val descriptors = mutable.HashMap.empty[DescriptorRef, Registration]

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[RegisterExtension])
    log.info(" started.")
  }

  override def postStop(): Unit = {
    log.info(" stopped.")
    eventBus.unsubscribe(self)
  }

  override def receive: Receive = {

    case RegisterExtension(extensionType, extensionClass) =>
      log.debug(s"Registering extension '$extensionClass' of type '$extensionType'.")

      extensionType match {
        case FilterExtension | SinkExtension | SourceExtension =>
          val descriptor = filterLoader.loadDescriptors(extensionClass)
          descriptors += (descriptor.ref -> Registration(descriptor, extensionClass))
      }

    case RequestDescriptors =>
      log.debug("Sending Descriptors.")
      sender ! DescriptorsResponse(descriptors.values.map(_.descriptor).toList)

    case CreateSinkStage(ref, stageContext, descriptor, configuration) =>
      log.debug(s"Creating SinkStage: ${descriptor.uuid}")

      descriptors.get(descriptor) match {
        case Some(registration) =>
          val provider = createSinkLogicProvider(registration.logicClass)
          val stage = new SinkStage(LogicParameters(ref, stageContext, configuration), provider)
          sender ! SinkStageCreated(ref, stage)
        case _ =>
          log.error(s"Could not create SinkStage: ${descriptor.uuid}")
      }

    case CreateSourceStage(ref, stageContext, descriptor, configuration) =>

      log.debug(s"Creating SourceStage: ${descriptor.uuid}")

      descriptors.get(descriptor) match {
        case Some(registration) =>
          val provider = createSourceLogicProvider(registration.logicClass)
          val stage = new SourceStage(LogicParameters(ref, stageContext, configuration), provider)
          sender ! SourceStageCreated(ref, stage)
        case _ =>
          log.error(s"Could not create SourceStage: ${descriptor.uuid}")
      }

    case CreateFilterStage(ref, stageContext, descriptor, configuration) =>

      log.debug(s"Creating FilterStage: ${descriptor.uuid}")

      descriptors.get(descriptor) match {
        case Some(registration) =>
          val provider = createFilterLogicProvider(registration.logicClass)
          val stage = new FilterStage(LogicParameters(ref, stageContext, configuration), provider)
          sender ! FilterStageCreated(ref, stage)
        case _ =>
          log.error(s"Could not create FilterStage: ${descriptor.uuid}")
      }

    case CreateBranchStage(ref, stageContext, descriptor, configuration) =>

      log.debug(s"Creating BranchStage: ${descriptor.uuid}")

      descriptors.get(descriptor) match {
        case Some(registration) =>
          val provider = createBranchLogicProvider(registration.logicClass)
          val stage = new BranchStage(LogicParameters(ref, stageContext, configuration), provider)
          sender ! BranchStageCreated(ref, stage)
        case _ =>
          log.error(s"Could not create BranchStage: ${descriptor.uuid}")
      }

    case CreateMergeStage(ref, stageContext, descriptor, configuration) =>

      log.debug(s"Creating MergeStage: ${descriptor.uuid}")

      descriptors.get(descriptor) match {
        case Some(registration) =>
          val provider = createMergeLogicProvider(registration.logicClass)
          val stage = new MergeStage(LogicParameters(ref, stageContext, configuration), provider)
          sender ! MergeStageCreated(ref, stage)
        case _ =>
          log.error(s"Could not create MergeStage: ${descriptor.uuid}")
      }

    case Ready =>
      sender ! Ready
  }
}
