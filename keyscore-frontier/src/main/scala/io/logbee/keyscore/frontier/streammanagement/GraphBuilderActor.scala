package streammanagement

import akka.{NotUsed, actor}
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.UniqueKillSwitch
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import io.logbee.keyscore.frontier.filter.{AddFieldsFilter, CommitableFilterMessage, FilterUtils}
import streammanagement.GraphBuilderActor.{BuildGraph, BuiltGraph}


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
