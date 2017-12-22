package streammanagement

import akka.{Done, NotUsed, actor}
import akka.actor.{Actor, ActorLogging, Cancellable, Props}
import akka.kafka.{ConsumerMessage, ProducerMessage}
import akka.kafka.scaladsl.Consumer
import akka.stream.{ClosedShape, FlowShape, KillSwitches, UniqueKillSwitch}
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import io.logbee.keyscore.frontier.filter.{AddFieldsFilter, CommitableFilterMessage, FilterUtils}
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


}

class GraphBuilderActor() extends Actor with ActorLogging {

  implicit val formats = org.json4s.DefaultFormats

  override def receive = {
    case BuildGraph(source, sink, flows) =>
      log.debug("building graph....")


      val finalSource = flows.foldLeft(source) { (currentSource, currentFlow) =>

        currentSource.viaMat(currentFlow)(Keep.left)
      }

      val graph = finalSource
        .viaMat(AddFieldsFilter(Map("akka_timestamp" -> FilterUtils.getCurrentTimeFormatted)))(Keep.left)
        .toMat(sink)(Keep.left)

      sender ! BuiltGraph(graph)
  }


}
