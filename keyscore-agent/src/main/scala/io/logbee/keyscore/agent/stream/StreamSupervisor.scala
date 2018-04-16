package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import akka.stream.UniqueKillSwitch
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.StreamSupervisor.{ChangeStream, CreateStream, ShutdownStream}
import io.logbee.keyscore.model.StreamModel

import scala.concurrent.duration._

object StreamSupervisor {
  case object StreamCreated
  case class StreamCreationError(errorMsg: String)
  case class StreamSupervisorError(errorMsg: String)
  case class CreateStream(streamId: UUID, streamSpec: StreamModel)
  case class ChangeStream(streamId: UUID, streamSpec: StreamModel)
  case class ShutdownStream(streamId: UUID)
}

class StreamSupervisor extends Actor with ActorLogging {

  implicit val timeout: Timeout = 30 seconds

  var killSwitch: Option[UniqueKillSwitch] = None

  override def preStart(): Unit = {
    log.info("Started StreamSupervisor.")
  }

  override def postStop(): Unit = {
    log.debug("Stopped StreamSupervisor.")
  }

  override def receive: Receive = {
    case CreateStream(streamId, streamSpec) =>
      log.info("Creating stream with id: "+streamId)

    case ChangeStream(streamId, streamSpec) =>
      log.info("Updating stream with id: "+streamId)

    case ShutdownStream(streamId) =>
      log.info("Shutting down stream with id: "+streamId)

  }
}
