package streammanagement

import akka.{Done, NotUsed, actor}
import akka.actor.{Actor, ActorLogging, Cancellable, Props}
import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.kafka.scaladsl.Consumer
import akka.stream.{ClosedShape, FlowShape, KillSwitches, UniqueKillSwitch}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import filter.CommitableFilterMessage
import streammanagement.GraphBuilderActor.{BuildGraph, BuiltGraph}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future


object GraphBuilderActor {
  def props(): Props = actor.Props(new GraphBuilderActor())

  case class BuildGraph(
                         source: Source[ConsumerMessage.CommittableMessage[Array[Byte], String],
                           Consumer.Control],
                         sink: SinkWithTopic,
                         flows: List[Flow[CommitableFilterMessage,CommitableFilterMessage,NotUsed]]
                       )

  case class BuiltGraph(graph: RunnableGraph[UniqueKillSwitch])

  case class SinkWithTopic(sink:Sink[ProducerMessage.Message[Array[Byte], String, ConsumerMessage.Committable],
    Future[Done]],topic:String)

}

class GraphBuilderActor() extends Actor with ActorLogging {


  override def receive = {
    case BuildGraph(source, sink, flows) =>
      log.info("building graph....")

      val startSource = source.map(msg => CommitableFilterMessage(msg.record.value(),msg.committableOffset)).viaMat(KillSwitches.single)(Keep.right)

      val finalSource = flows.foldLeft(startSource) { (currentSource, currentFlow) =>

        currentSource.viaMat(currentFlow)(Keep.left)
      }

      val graph = finalSource.map { msg =>
        ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
          sink.topic, msg.value
        ), msg.committableOffset)
      }.toMat(sink.sink)(Keep.left)

      sender ! BuiltGraph(graph)
  }


}
