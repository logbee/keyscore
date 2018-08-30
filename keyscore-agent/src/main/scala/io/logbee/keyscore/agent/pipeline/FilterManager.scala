package io.logbee.keyscore.agent.pipeline

import akka.actor
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import io.logbee.keyscore.agent.pipeline.FilterManager._
import io.logbee.keyscore.agent.pipeline.stage._
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.{FilterExtension, SinkExtension, SourceExtension}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.configuration.Configuration
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
          descriptors += (descriptor.uuid -> Registration(descriptor, extensionClass))
      }

    case RequestDescriptors =>
      sender ! DescriptorsResponse(descriptors.values.map(_.filterDescriptor).toList)

//    case CreateSinkStage(stageContext, configuration) =>
//
//      log.info(s"Creating SinkStage with: $configuration")
//
//      loadStageLogicClass(configuration.descriptor.name).foreach(logicClass => {
//        val provider = createSinkLogicProvider(logicClass)
//        val stage = new SinkStage(stageContext, configuration, provider)
//
//        sender ! SinkStageCreated(stage)
//      })
//
//    case CreateSourceStage(stageContext, configuration) =>
//
//      log.info(s"Creating SourceStage with: $configuration")
//
//      loadStageLogicClass(configuration.descriptor.name).foreach(logicClass => {
//        log.info("Filtermanager: loadSourceStageLogicClass")
//        val provider = createSourceLogicProvider(logicClass)
//        val stage = new SourceStage(stageContext, configuration, provider)
//
//        sender ! SourceStageCreated(stage)
//      })
//    case CreateFilterStage(stageContext, configuration) =>
//
//      log.info(s"Creating FilterStage with: $configuration")
//
//      loadStageLogicClass(configuration.descriptor.name).foreach(logicClass => {
//        val provider = createFilterLogicProvider(logicClass)
//        val stage = new FilterStage(stageContext, configuration, provider)
//
//        sender ! FilterStageCreated(stage)
//      })

    case Ready =>
      sender ! Ready
  }

//  private def createSinkLogicProvider(logicClass: Class[_]) = {
//    val constructor = getSinkStageLogicConstructor(logicClass)
//    (context: StageContext, configuration: FilterConfiguration, shape: SinkShape[Dataset]) => {
//      constructor.newInstance(context,configuration,shape).asInstanceOf[SinkLogic]
//    }
//  }
//
//  private def createSourceLogicProvider(logicClass: Class[_]) = {
//    val constructor = getSourceStageLogicConstructor(logicClass)
//    (context: StageContext, configuration: FilterConfiguration, shape: SourceShape[Dataset]) => {
//      constructor.newInstance(context, configuration, shape).asInstanceOf[SourceLogic]
//    }
//  }
//
//  private def createFilterLogicProvider(logicClass: Class[_]) = {
//    val constructor = getFilterStageLogicConstructor(logicClass)
//    (context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) => {
//      constructor.newInstance(context, configuration, shape).asInstanceOf[FilterLogic]
//    }
//  }

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

  private def loadStageLogicClass(className: String) = {
    log.info(s"Loading StageLogic class: $className")
    Option(getClass.getClassLoader.loadClass(className))
  }
}
