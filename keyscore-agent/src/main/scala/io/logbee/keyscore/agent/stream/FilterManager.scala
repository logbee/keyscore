package io.logbee.keyscore.agent.stream

import akka.actor
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.{ActorMaterializer, SinkShape}
import io.logbee.keyscore.agent.stream.FilterManager._
import io.logbee.keyscore.agent.stream.stage.{SinkStage, StageContext}
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.{FilterExtension, SinkExtension, SourceExtension}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, MetaFilterDescriptor}

import scala.collection.mutable
import scala.reflect.internal.util.ScalaClassLoader


object FilterManager {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new FilterManager)

  case object RequestDescriptors

  case class DescriptorsResponse(descriptors: List[MetaFilterDescriptor])

  case class CreateSinkStage(context: StageContext, configuration: FilterConfiguration)

  case class SinkStageCreated(stage: SinkStage)
}

class FilterManager extends Actor with ActorLogging {

  private val eventBus = context.system.eventStream
  private val filterLoader = new FilterLoader

  private val descriptors = mutable.HashMap.empty[String, FilterRegistration]

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[RegisterExtension])
    log.info("StartUp complete.")
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
          descriptors += (descriptor.name -> FilterRegistration(descriptor, extensionClass))
      }

    case RequestDescriptors =>
      sender ! DescriptorsResponse(descriptors.values.map(_.filterDescriptor).toList)

    case CreateSinkStage(stageContext, configuration) =>

      log.debug(s"Creating SinkStage with: $configuration")

      val className = configuration.descriptor.name

      ScalaClassLoader(getClass.getClassLoader).tryToLoadClass(className) match {

        case Some(logicClass) =>

          val constructor = logicClass.getConstructor(classOf[StageContext], classOf[FilterConfiguration], classOf[SinkShape[Dataset]])
          val stage = new SinkStage(stageContext, configuration, (context: StageContext, configuration: FilterConfiguration, shape: SinkShape[Dataset]) => {
            constructor.newInstance(context, configuration, shape)
          })

          sender ! SinkStageCreated(stage)

          log.info(s"SinkStage created: $stage")

        case None =>
          log.error(s"Could not load stage class: $className")
      }

    case Ready =>
      sender ! Ready
  }
}
