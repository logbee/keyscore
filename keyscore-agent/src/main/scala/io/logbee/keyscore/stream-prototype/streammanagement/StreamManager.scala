package streammanagement

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import filter.CommitableFilterMessage
import streammanagement.GraphBuilderActor.SinkWithTopic
import streammanagement.RunningStreamActor.ShutdownGraph
import streammanagement.StreamManager.{ChangeStream, CreateNewStream}


object StreamManager {
  def props(implicit materializer: ActorMaterializer): Props = Props(new StreamManager)

  case class ChangeStream(id: Int,
                          source: Source[ConsumerMessage.CommittableMessage[Array[Byte], String],
                            Consumer.Control],
                          sink: SinkWithTopic,
                          flows: List[Flow[CommitableFilterMessage,CommitableFilterMessage,NotUsed]])

  case class CreateNewStream(id: Int,
                             source: Source[ConsumerMessage.CommittableMessage[Array[Byte], String],
                               Consumer.Control],
                             sink: SinkWithTopic,
                             flows: List[Flow[CommitableFilterMessage,CommitableFilterMessage,NotUsed]])

}

class StreamManager(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  var idToActor = Map.empty[Int, ActorRef]
  var actorToId = Map.empty[ActorRef, Int]


  override def preStart(): Unit = {
    log.info("StreamManager started")
  }

  override def postStop(): Unit = {
    log.info("StreamManager stopped")
  }

  override def receive = {
    case CreateNewStream(streamId, source, sink, flows) =>
      idToActor.get(streamId) match {
        case Some(_) =>
          self ! ChangeStream(streamId, source, sink, flows)
        case None =>
          val streamActor = context.actorOf(RunningStreamActor.props(source, sink, flows))
          idToActor += streamId -> streamActor
          actorToId += streamActor -> streamId
      }
    case ChangeStream(streamId, source, sink, flows) =>
      val streamActor = idToActor(streamId)
      actorToId -= streamActor
      idToActor -= streamId
      streamActor ! ShutdownGraph
      val newStreamActor = context.actorOf(RunningStreamActor.props(source, sink, flows))
      idToActor += streamId -> newStreamActor
      actorToId += newStreamActor -> streamId
  }
}
