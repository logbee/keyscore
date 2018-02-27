package io.logbee.keyscore.frontier.stream

import java.util.UUID

import _root_.streammanagement.StreamSupervisor.ShutdownGraph
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import io.logbee.keyscore.frontier.stream.StreamManager._
import io.logbee.keyscore.model._
import streammanagement.FilterManager.BuildGraphException
import streammanagement.StreamSupervisor
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._


object StreamManager {
  def props(filterManager: ActorRef)(implicit materializer: ActorMaterializer): Props = Props(new StreamManager(filterManager))

  case class TranslateAndCreateNewStream(streamId: UUID, streamModel: StreamModel)


  case class ChangeStream(streamId: UUID, stream: StreamModel)

  case class CreateNewStream(streamId: UUID, stream: StreamModel)

  case class StreamCreatedWithID(id: UUID)

  case class StreamUpdated(id: UUID)

  case class DeleteStream(id: UUID)

  case class StreamDeleted(id: UUID)

  case class StreamNotFound(id: UUID)

}

class StreamManager(filterManager: ActorRef)(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  implicit val timeout: Timeout = 2 seconds
  var idToActor = Map.empty[UUID, ActorRef]
  var actorToId = Map.empty[ActorRef, UUID]

  override def preStart(): Unit = {
    log.info("StreamManager started")
  }

  override def postStop(): Unit = {
    log.info("StreamManager stopped")
  }

  override def receive: Receive = {
    case CreateNewStream(streamId, stream) =>
      idToActor.get(streamId) match {
        case Some(_) =>
          self tell(ChangeStream(streamId, stream), sender())
        case None =>
          val streamActor = context.actorOf(StreamSupervisor.props(filterManager))
          val createAnswer = Await.result(streamActor ? CreateNewStream(streamId, stream), 2 seconds)
          createAnswer match {
            case StreamCreatedWithID =>
              addStreamActor(streamId, streamActor)
              sender ! createAnswer
            case _ => sender ! createAnswer
          }


      }

    case ChangeStream(streamId, stream) =>

      val newStreamActor = context.actorOf(StreamSupervisor.props(filterManager))
      val updateAnswer = Await.result(newStreamActor ? CreateNewStream(streamId, stream), 2 seconds)
      updateAnswer match {
        case StreamCreatedWithID(streamId) =>
          val streamActor: ActorRef = removeStreamActor(streamId)
          streamActor ! ShutdownGraph
          addStreamActor(streamId, newStreamActor)
          sender ! StreamUpdated(streamId)
        case otherAnswer => sender ! otherAnswer
      }

    case DeleteStream(streamId) =>
      idToActor.get(streamId) match {
        case Some(actor) =>
          actor ! ShutdownGraph
          sender ! StreamDeleted(streamId)
          removeStreamActor(streamId)
        case _ => sender() ! StreamNotFound(streamId)
      }

    case StreamCreatedWithID(streamId) =>
      context.parent ! StreamCreatedWithID(streamId)
    case BuildGraphException(id, model, msg) =>
      context.parent ! BuildGraphException(id, model, msg)
  }

  private def addStreamActor(streamId: UUID, actor: ActorRef): Unit = {
    idToActor += streamId -> actor
    actorToId += actor -> streamId
  }

  private def removeStreamActor(streamId: UUID): ActorRef = {
    val streamActor = idToActor(streamId)
    actorToId -= streamActor
    idToActor -= streamId
    streamActor
  }
}
