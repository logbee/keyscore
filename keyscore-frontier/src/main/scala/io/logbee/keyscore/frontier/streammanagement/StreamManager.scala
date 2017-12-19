package streammanagement

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import filter.CommitableFilterMessage
import streammanagement.RunningStreamActor.ShutdownGraph
import streammanagement.StreamManager.{ChangeStream, CreateNewStream}


object StreamManager {
  def props(implicit materializer: ActorMaterializer): Props = Props(new StreamManager)

  case class Stream(uuid: UUID,
                    source: Source[CommitableFilterMessage, UniqueKillSwitch],
                    sink: Sink[CommitableFilterMessage, NotUsed],
                    flows: List[Flow[CommitableFilterMessage,CommitableFilterMessage,NotUsed]])

  case class ChangeStream(stream:Stream)

  case class CreateNewStream(stream:Stream)

}

class StreamManager(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  var idToActor = Map.empty[UUID, ActorRef]
  var actorToId = Map.empty[ActorRef, UUID]


  override def preStart(): Unit = {
    log.info("StreamManager started")
  }

  override def postStop(): Unit = {
    log.info("StreamManager stopped")
  }

  override def receive = {
    case CreateNewStream(stream) =>
      idToActor.get(stream.uuid) match {
        case Some(_) =>
          self ! ChangeStream(stream)
        case None =>
          val streamActor = context.actorOf(RunningStreamActor.props(stream.source, stream.sink, stream.flows))
          idToActor += stream.uuid -> streamActor
          actorToId += streamActor -> stream.uuid
      }
    case ChangeStream(stream) =>
      val streamActor = idToActor(stream.uuid)
      actorToId -= streamActor
      idToActor -= stream.uuid
      streamActor ! ShutdownGraph
      val newStreamActor = context.actorOf(RunningStreamActor.props(stream.source, stream.sink, stream.flows))
      idToActor += stream.uuid -> newStreamActor
      actorToId += newStreamActor -> stream.uuid
  }
}
