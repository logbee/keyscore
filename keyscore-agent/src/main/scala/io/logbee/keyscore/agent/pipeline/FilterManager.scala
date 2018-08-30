package io.logbee.keyscore.agent.pipeline

import akka.actor
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, SourceShape}
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.stage._
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.{FilterExtension, SinkExtension, SourceExtension}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}

import scala.collection.mutable


object FilterManager {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new FilterManager)

  case object RequestDescriptors

  case class DescriptorsResponse(descriptors: List[Descriptor])

  case class CreateSinkStage(context: StageContext, descriptor: Descriptor, configuration: Configuration)

  case class CreateSourceStage(context: StageContext, descriptor: Descriptor, configuration: Configuration)

  case class CreateFilterStage(context: StageContext, descriptor: Descriptor, configuration: Configuration)

  case class SinkStageCreated(stage: SinkStage)

  case class SourceStageCreated(stage: SourceStage)

  case class FilterStageCreated(stage: FilterStage)

}

class FilterManager extends Actor with ActorLogging {

  private val eventBus = context.system.eventStream
  private val filterLoader = new FilterLoader

  private val descriptors = mutable.HashMap.empty[DescriptorRef, Registration]

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[RegisterExtension])
    log.info("[Filtermanager]: StartUp complete.")
  }

  override def postStop(): Unit = {
    eventBus.unsubscribe(self)
  }

  override def receive: Receive = {

    case RegisterExtension(extensionType, extensionClass) =>
      log.info(s"Registering extension '$extensionClass' of type '$extensionType'.")
      extensionType match {
        case FilterExtension | SinkExtension | SourceExtension =>
          val descriptor = filterLoader.loadDescriptors(extensionClass)
          descriptors += (descriptor.ref -> Registration(descriptor, extensionClass))
      }

    case RequestDescriptors =>
      sender ! DescriptorsResponse(descriptors.values.map(_.filterDescriptor).toList)

    case CreateSinkStage(stageContext, descriptor, configuration) =>

      log.info(s"Creating SinkStage: ${descriptor.ref.uuid}")

      descriptors.get(descriptor.ref) match {
        case Some(registration) =>
          val provider = createSinkLogicProvider(registration.logicClass)
          val stage = new SinkStage(stageContext, configuration, provider)
          sender ! SinkStageCreated(stage)
        case _ =>
          log.error(s"Could not create SinkStage: ${descriptor.ref.uuid}")
      }

    case CreateSourceStage(stageContext, descriptor, configuration) =>

      log.info(s"Creating SourceStage: ${descriptor.ref.uuid}")

      descriptors.get(descriptor.ref) match {
        case Some(registration) =>
          val provider = createSourceLogicProvider(registration.logicClass)
          val stage = new SourceStage(stageContext, configuration, provider)
          sender ! SourceStageCreated(stage)
        case _ =>
          log.error(s"Could not create SourceStage: ${descriptor.ref.uuid}")
      }

    case CreateFilterStage(stageContext, descriptor, configuration) =>

      log.info(s"Creating FilterStage: ${descriptor.ref.uuid}")

      descriptors.get(descriptor.ref) match {
        case Some(registration) =>
          val provider = createFilterLogicProvider(registration.logicClass)
          val stage = new FilterStage(stageContext, configuration, provider)

          sender ! FilterStageCreated(stage)
        case _ =>
          log.error(s"Could not create SourceStage: ${descriptor.ref.uuid}")

      }

    case Ready =>
      sender ! Ready
  }

  private def createSinkLogicProvider(logicClass: Class[_]) = {
    val constructor = getSinkStageLogicConstructor(logicClass)
    (context: StageContext, configuration: Configuration, shape: SinkShape[Dataset]) => {
      constructor.newInstance(context, configuration, shape).asInstanceOf[SinkLogic]
    }
  }

  private def createSourceLogicProvider(logicClass: Class[_]) = {
    val constructor = getSourceStageLogicConstructor(logicClass)
    (context: StageContext, configuration: Configuration, shape: SourceShape[Dataset]) => {
      constructor.newInstance(context, configuration, shape).asInstanceOf[SourceLogic]
    }
  }

  private def createFilterLogicProvider(logicClass: Class[_]) = {
    val constructor = getFilterStageLogicConstructor(logicClass)
    (context: StageContext, configuration: Configuration, shape: FlowShape[Dataset, Dataset]) => {
      constructor.newInstance(context, configuration, shape).asInstanceOf[FilterLogic]
    }
  }

  private def getSinkStageLogicConstructor(logicClass: Class[_]) = {
    log.info("FilterManager " + logicClass)
    logicClass.getConstructors()(0)
  }

  private def getSourceStageLogicConstructor(logicClass: Class[_]) = {
    logicClass.getConstructors()(0)
  }

  private def getFilterStageLogicConstructor(logicClass: Class[_]) = {
    logicClass.getConstructors()(0)
  }

}
