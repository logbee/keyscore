package io.logbee.keyscore.agent.stream.management

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.management.StreamManager._
import io.logbee.keyscore.model.StreamConfiguration

import scala.concurrent.duration._
import scala.language.postfixOps

object StreamManager {

  private val SUPERVISOR_NAME_PREFIX = "stream:"

  case class CreateStream(configuration: StreamConfiguration)

  case class UpdateStream(configuration: StreamConfiguration)

  case class DeleteStream(configuration: StreamConfiguration)

  case object GetConfigurations
}

class StreamManager extends Actor with ActorLogging {

  implicit val timeout: Timeout = 10 seconds

  import context._

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case createStream @ CreateStream(configuration) =>
      val supervisor = actorOf(StreamSupervisor(configuration.id), nameFromModel(configuration))
      supervisor ! createStream
      sender ! supervisor

    case updateStream @ UpdateStream(configuration) =>
      child(nameFromModel(configuration)).foreach(child => child ! updateStream)

    case deleteStream @ DeleteStream(configuration) =>
      child(nameFromModel(configuration)).foreach(child => child ! deleteStream)

    case GetConfigurations =>
      actorOf(StreamConfigurationAggregator(sender, children.filter(isSupervisor)))

    case _ =>
  }

  private def nameFromModel(model: StreamConfiguration): String = {
    s"$SUPERVISOR_NAME_PREFIX${model.id}"
  }

  private def isSupervisor(ref: ActorRef): Boolean = {
    ref.path.name.startsWith(SUPERVISOR_NAME_PREFIX)
  }
}
