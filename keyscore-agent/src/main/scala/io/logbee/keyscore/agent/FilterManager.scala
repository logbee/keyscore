package io.logbee.keyscore.agent

import akka.actor.{Actor, ActorLogging}
import io.logbee.keyscore.agent.FilterManager.{Descriptors, GetDescriptors}
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.{FilterExtension, SinkExtension, SourceExtension}
import io.logbee.keyscore.commons.util.StartUpWatch.Ready
import io.logbee.keyscore.model.filter.FilterDescriptor

import scala.collection.mutable

object FilterManager {
  case object GetDescriptors
  case class Descriptors(descriptors: List[FilterDescriptor])
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
          val descriptor = filterLoader.loadDescriptor(extensionClass)
          descriptors += (descriptor.name -> FilterRegistration(descriptor, extensionClass))
      }
    case GetDescriptors =>
      sender ! Descriptors(descriptors.values.map(_.filterDescriptor).toList)
    case Ready =>
      sender ! Ready
  }
}

case class FilterRegistration(filterDescriptor: FilterDescriptor, filterClass: Class[_])
