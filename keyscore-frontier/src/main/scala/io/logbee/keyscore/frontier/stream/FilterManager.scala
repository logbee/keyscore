package streammanagement

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.{Done, NotUsed, actor}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import io.logbee.keyscore.frontier.util.Reflection
import io.logbee.keyscore.model.StreamModel
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink.{KafkaSinkModel, SinkTypes}
import io.logbee.keyscore.model.source.{KafkaSourceModel, SourceTypes}
import streammanagement.FilterManager._

import scala.util.control.Breaks._
import scala.reflect.runtime.{universe => ru}
import scala.concurrent.Future
import scala.util.Success

object FilterManager {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new FilterManager)

  trait BuildGraphAnswer{

  }

  case class BuildGraphAnswerWrapper(answer:Option[BuildGraphAnswer])

  case class BuildGraph(streamId: UUID, stream: StreamModel)

  case class BuiltGraph (streamId:UUID,graph: RunnableGraph[UniqueKillSwitch]) extends BuildGraphAnswer

  case class BuildGraphException(streamId: UUID, stream: StreamModel, msg: String) extends BuildGraphAnswer


  case class StreamBlueprint(uuid: UUID,
                             source: Source[CommittableRecord, UniqueKillSwitch],
                             sink: Sink[CommittableRecord, Future[Done]],
                             filter: Map[UUID, Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]]])

  case class UpdateFilter(uuid: UUID, configuration: GrokFilterConfiguration)

  case class FilterUpdated(id: UUID)

  case class FilterNotFound(id: UUID)

}

class FilterManager(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val formats = org.json4s.DefaultFormats
  implicit val system = context.system
  implicit val executionContext = context.dispatcher

  val filterHandles: scala.collection.mutable.Map[UUID, Future[FilterHandle]] = scala.collection.mutable.Map()

  override def receive = {
    case BuildGraph(streamId, stream) =>
      breakable {
        log.info("building graph....")

        //TODO: solve this in a proper "scala way"
        val streamBlueprint = try {
          createStreamFromModel(streamId, stream)
        } catch {
          case nse: NoSuchElementException =>
            sender ! BuildGraphAnswerWrapper(Some(BuildGraphException(streamId, stream, nse.getMessage)))
            break
        }


        val finalSource = streamBlueprint.filter.foldLeft(streamBlueprint.source) { (currentSource, currentFlow) =>
          currentSource.viaMat(currentFlow._2) { (matLeft, matRight) =>
            println(matRight.toString)
            filterHandles += ((currentFlow._1, matRight))
            matLeft
          }
        }

        val graph = finalSource
          .viaMat(AddFieldsFilter(Map("keyscore_timestamp" -> FilterUtils.getCurrentTimeFormatted)))(Keep.left)
          .toMat(streamBlueprint.sink)(Keep.left)

        sender ! BuildGraphAnswerWrapper(Some(BuiltGraph(streamId,graph)))
      }


    case UpdateFilter(uuid, config) =>
      filterHandles.get(uuid) match {
        case Some(future) =>
          future.mapTo[GrokFilterHandle].onComplete {
            case Success(handle) =>
              handle.configure(config)
          }
          sender ! FilterUpdated(uuid)
        case _ =>
          sender ! FilterNotFound(uuid)
      }

  }

  private def createStreamFromModel(streamId: UUID, model: StreamModel): StreamBlueprint = {
    try {
      val source: Source[CommittableRecord, UniqueKillSwitch] =
        Reflection.createFilterByClassname(FilterRegistry.filters(model.source.kind), model.source, Some(system)).asInstanceOf[Source[CommittableRecord, UniqueKillSwitch]]

      val sink: Sink[CommittableRecord, Future[Done]] =
        Reflection.createFilterByClassname(FilterRegistry.filters(model.sink.kind), model.sink, Some(system)).asInstanceOf[Sink[CommittableRecord, Future[Done]]]

      val filterBuffer = scala.collection.mutable.Map[UUID, Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]]]()

      model.filter.foreach { filter =>
        filterBuffer += ((filter.id, Reflection.createFilterByClassname(FilterRegistry.filters(filter.kind), filter).asInstanceOf[Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]]]))
      }


      StreamBlueprint(streamId, source, sink, filterBuffer.toMap)
    } catch {
      case nse: NoSuchElementException => throw nse
    }
  }


}

object FilterRegistry {

  val filters = Map(
    "KafkaSource" -> "io.logbee.keyscore.frontier.sources.KafkaSource",
    "HttpSource" -> "io.logbee.keyscore.frontier.sources.HttpSource",
    "KafkaSink" -> "io.logbee.keyscore.frontier.sinks.KafkaSink",
    "StdOutSink" -> "io.logbee.keyscore.frontier.sinks.StdOutSink",
    "AddFieldsFilter" -> "io.logbee.keyscore.frontier.filters.AddFieldsFilter",
    "RemoveFieldsFilter" -> "io.logbee.keyscore.frontier.filters.RemoveFieldsFilter",
    "RetainFieldsFilter" -> "io.logbee.keyscore.frontier.filters.RetainFieldsFilter",
    "GrokFilter" -> "io.logbee.keyscore.frontier.filters.GrokFilter"

  )
}
