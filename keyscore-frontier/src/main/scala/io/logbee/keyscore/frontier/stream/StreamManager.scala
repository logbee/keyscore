package io.logbee.keyscore.frontier.stream

import java.util.UUID

import _root_.streammanagement.RunningStreamActor.ShutdownGraph
import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import io.logbee.keyscore.frontier.stream.StreamManager._
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink.{KafkaSinkModel, SinkTypes}
import io.logbee.keyscore.model.source.{KafkaSourceModel, SourceTypes}
import streammanagement.RunningStreamActor

import scala.concurrent.Future


object StreamManager {
  def props(filterManager:ActorRef)(implicit materializer: ActorMaterializer): Props = Props(new StreamManager(filterManager))

  case class TranslateAndCreateNewStream(streamId: UUID, streamModel: StreamModel)


  case class ChangeStream(streamId: UUID, stream: StreamModel)

  case class CreateNewStream(streamId: UUID, stream: StreamModel)

  case class StreamCreatedWithID(id: UUID)

  case class StreamUpdated(id: UUID)

  case class DeleteStream(id: UUID)

  case class StreamDeleted(id: UUID)

  case class StreamNotFound(id: UUID)

}

class StreamManager(filterManager:ActorRef)(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
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
          self tell(ChangeStream(streamId,stream), sender())
        case None =>
          val streamActor = context.actorOf(RunningStreamActor.props(streamId,stream,filterManager))
          addStreamActor(streamId, streamActor)
          sender() ! StreamCreatedWithID(streamId)
      }

    case ChangeStream(streamId, stream) =>
      val streamActor: ActorRef = removeStreamActor(streamId)
      streamActor ! ShutdownGraph
      val newStreamActor = context.actorOf(RunningStreamActor.props(streamId,stream,filterManager))
      addStreamActor(streamId, newStreamActor)
      sender() ! StreamUpdated(streamId)

    case DeleteStream(streamId) =>
      idToActor.get(streamId) match {
        case Some(actor) =>
          actor ! ShutdownGraph
          sender ! StreamDeleted(streamId)
          removeStreamActor(streamId)
        case _ => sender() ! StreamNotFound(streamId)
      }
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
