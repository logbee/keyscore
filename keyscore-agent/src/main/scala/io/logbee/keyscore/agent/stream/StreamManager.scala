package io.logbee.keyscore.agent.stream

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.StreamManager._
import io.logbee.keyscore.agent.stream.StreamSupervisor.{ConfigureStream, StartStream}
import io.logbee.keyscore.model.StreamConfiguration

import scala.concurrent.duration._
import scala.language.postfixOps

object StreamManager {

  private val SUPERVISOR_NAME_PREFIX = "stream:"

  case class CreateStream(configuration: StreamConfiguration)

  case class UpdateStream(configuration: StreamConfiguration)

  case class DeleteStream(configuration: StreamConfiguration)

  case object RequestStreamsState

  private case class SupervisorTerminated(supervisor: ActorRef, configuration: StreamConfiguration)

  def apply(filterManager: ActorRef): Props = Props(new StreamManager(filterManager))
}

class StreamManager(filterManager: ActorRef) extends Actor with ActorLogging {

  implicit val timeout: Timeout = 10 seconds

  import context._

  override def preStart(): Unit = {
    log.info("StartUp complete.")
  }

  override def receive: Receive = {

    case CreateStream(configuration) =>
      val supervisor = actorOf(StreamSupervisor(filterManager), nameFromConfiguration(configuration))
      supervisor ! StartStream(configuration)
      watchWith(supervisor, SupervisorTerminated(supervisor, configuration))

    case UpdateStream(configuration) =>
      child(nameFromConfiguration(configuration)).foreach(child => child ! ConfigureStream(configuration))

    case DeleteStream(configuration) =>
      child(nameFromConfiguration(configuration)).foreach(child => context.stop(child))

    case RequestStreamsState =>
      actorOf(StreamStateAggregator(sender, children.filter(isSupervisor)))

    case SupervisorTerminated(supervisor, configuration) =>
      log.info(s"StreamSupervisor terminated: $configuration")

    case _ =>
  }

  def nameFromConfiguration(configuration: StreamConfiguration): String = {
    s"$SUPERVISOR_NAME_PREFIX${configuration.id}"
  }

  def isSupervisor(ref: ActorRef): Boolean = {
    ref.path.name.startsWith(SUPERVISOR_NAME_PREFIX)
  }
}
