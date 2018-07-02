package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.agent.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.agent.extension.ExternalFilterExtension
import io.logbee.keyscore.commons.util.StartUpWatch.Ready

object ExternalFilterManager {

  def apply(filterManager: ActorRef): Props = Props(new ExternalFilterManager(filterManager))
}

class ExternalFilterManager(filterManager: ActorRef) extends Actor with ActorLogging {

  private val eventBus = context.system.eventStream

  override def preStart(): Unit = {
    eventBus.subscribe(self, classOf[RegisterExtension])
    log.info("[ExternalFilterManager]: StartUp complete.")
  }

  override def postStop(): Unit = {
    eventBus.unsubscribe(self)
  }

  override def receive: Receive = {

    case RegisterExtension(`ExternalFilterExtension`, extensionClass, extensionConfiguration) =>


    case Ready =>
      sender ! Ready
  }
}
