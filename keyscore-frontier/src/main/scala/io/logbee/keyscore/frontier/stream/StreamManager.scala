package io.logbee.keyscore.frontier.stream

import java.util.UUID

import _root_.streammanagement.StreamSupervisor.ShutdownGraph
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.logbee.keyscore.frontier.stream.StreamManager._
import io.logbee.keyscore.model._
import streammanagement.FilterManager.BuildGraphException
import streammanagement.StreamSupervisor

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}


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

  case object GetAllStreams

  case class RunningStreams(streamsList: Set[UUID])
}

class StreamManager(filterManager: ActorRef)(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  implicit val executionContext = context.dispatcher
  //TODO For testing docker-kafka only
  implicit val timeout: Timeout = 30 seconds
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
          //TODO For testing docker-kafka only
          val currentSender = sender()
          (streamActor ? CreateNewStream(streamId, stream)).onComplete {
            case Success(message) =>
              message match {
                case StreamCreatedWithID =>
                  addStreamActor(streamId, streamActor)
                  currentSender ! message
                case _ => currentSender ! message
              }
            case Failure(e) =>
          }
      }

    case ChangeStream(streamId, stream) =>

      val newStreamActor = context.actorOf(StreamSupervisor.props(filterManager))
      //TODO For testing docker-kafka only
      val currentSender = sender()
      (newStreamActor ? CreateNewStream(streamId, stream)).onComplete {
        case Success(updateAnswer) =>
          updateAnswer match {
            case StreamCreatedWithID(streamId) =>
              val streamActor: ActorRef = removeStreamActor(streamId)
              streamActor ! ShutdownGraph
              addStreamActor(streamId, newStreamActor)
              currentSender ! StreamUpdated(streamId)
            case otherAnswer => currentSender ! otherAnswer
          }
        case Failure(e) =>
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
    case GetAllStreams =>
      sender ! RunningStreams(idToActor.keySet)
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
