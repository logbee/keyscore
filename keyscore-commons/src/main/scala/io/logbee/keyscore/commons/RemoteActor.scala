package io.logbee.keyscore.commons

import akka.actor.{Actor, ActorLogging}
import io.logbee.keyscore.commons.RemoteActor.RemoteTry

object RemoteActor {
  case object RemoteTry
}

class RemoteActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case RemoteTry =>
      log.info("Creation successfull.")
  }
}
