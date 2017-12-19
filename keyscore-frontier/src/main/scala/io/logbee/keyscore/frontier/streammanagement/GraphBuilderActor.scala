package streammanagement

import akka.{Done, NotUsed, actor}
import akka.actor.{Actor, ActorLogging, Cancellable, Props}
import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.kafka.scaladsl.Consumer
import akka.stream.{ClosedShape, FlowShape, KillSwitches, UniqueKillSwitch}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import filter.{AddFieldsFilter, CommitableFilterMessage, FilterUtils}
import streammanagement.GraphBuilderActor.{BuildGraph, BuiltGraph}
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization

import scala.concurrent.Future


object GraphBuilderActor {
  def props(): Props = actor.Props(new GraphBuilderActor())

  case class BuildGraph(
                         source: Source[CommitableFilterMessage, UniqueKillSwitch],
                         sink: Sink[CommitableFilterMessage, NotUsed],
                         flows: List[Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed]]
                       )

  case class BuiltGraph(graph: RunnableGraph[UniqueKillSwitch])

  case class SinkWithTopic(sink: Sink[ProducerMessage.Message[Array[Byte], String, ConsumerMessage.Committable],
    Future[Done]], topic: String)

}

class GraphBuilderActor() extends Actor with ActorLogging {

  implicit val formats = org.json4s.DefaultFormats

  override def receive = {
    case BuildGraph(source, sink, flows) =>
      log.debug("building graph....")

      val startSource = source.map { msg =>
        val msgMap = parse(msg.record.value()).extract[Map[String, String]]
        CommitableFilterMessage(msgMap, msg.committableOffset)
      }.viaMat(KillSwitches.single)(Keep.right)

      val finalSource = flows.foldLeft(startSource) { (currentSource, currentFlow) =>

        currentSource.viaMat(currentFlow)(Keep.left)
      }

      val graph = finalSource
        .viaMat(AddFieldsFilter(Map("akka_timestamp" -> FilterUtils.getCurrentTimeFormatted)))(Keep.left)
        .map { msg =>

          val msgString = Serialization.write(msg.value)
          ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
            sink.topic, msgString
          ), msg.committableOffset)
        }.toMat(sink.sink)(Keep.left)

      sender ! BuiltGraph(graph)
  }


}
