package streammanagement

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.{NotUsed, actor}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import io.logbee.keyscore.frontier.util.Reflection
import io.logbee.keyscore.model.StreamModel
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink.{KafkaSinkModel, SinkTypes}
import io.logbee.keyscore.model.source.{KafkaSourceModel, SourceTypes}
import streammanagement.FilterManager._

import scala.reflect.runtime.{universe => ru}
import scala.concurrent.Future
import scala.util.Success

object FilterManager {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new FilterManager)

  case class BuildGraph(streamId: UUID, stream: StreamModel)

  case class BuiltGraph(graph: RunnableGraph[UniqueKillSwitch])


  case class StreamBlueprint(uuid: UUID,
                             source: Source[CommittableRecord, UniqueKillSwitch],
                             sink: Sink[CommittableRecord, NotUsed],
                             filter: Map[UUID, Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]]])

  case class UpdateFilter(uuid:UUID,configuration:GrokFilterConfiguration)

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

      log.info("building graph....")

      val streamBlueprint = createStreamFromModel(streamId, stream)

      val finalSource = streamBlueprint.filter.foldLeft(streamBlueprint.source) { (currentSource, currentFlow) =>
        currentSource.viaMat(currentFlow._2) { (matLeft, matRight) =>
          println(matRight.toString)
          filterHandles += ((currentFlow._1, matRight))
          matLeft
        }
      }

      val graph = finalSource
        .viaMat(AddFieldsFilter(Map("akka_timestamp" -> FilterUtils.getCurrentTimeFormatted)))(Keep.left)
        .toMat(streamBlueprint.sink)(Keep.left)

      sender ! BuiltGraph(graph)

    case UpdateFilter(uuid,config)=>
      filterHandles.get(uuid) match{
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

    val source:Source[CommittableRecord,UniqueKillSwitch] =
      Reflection.createFilterByClassname(model.source.kind,"create",model.source).asInstanceOf[Source[CommittableRecord,UniqueKillSwitch]]

    val sink:Sink[CommittableRecord,NotUsed] =
      Reflection.createFilterByClassname(model.sink.kind,"create",model.sink).asInstanceOf[Sink[CommittableRecord,NotUsed]]

    val filterBuffer = scala.collection.mutable.Map[UUID, Flow[CommittableRecord, CommittableRecord, Future[FilterHandle]]]()

    model.filter.foreach{filter =>
      filterBuffer += ((filter.id,Reflection.createFilterByClassname(filter.kind,"apply",filter).asInstanceOf[Flow[CommittableRecord,CommittableRecord,Future[FilterHandle]]]))
    }

    /*model.filter.foreach { filter =>
      filter.filter_type match {
        case FilterTypes.RetainFields => filterBuffer += ((filter.filter_id, RetainFieldsFilter(filter.asInstanceOf[RetainFieldsFilterModel].fields_to_retain)))
        case FilterTypes.AddFields => filterBuffer += ((filter.filter_id, AddFieldsFilter(filter.asInstanceOf[AddFieldsFilterModel].fields_to_add)))
        case FilterTypes.RemoveFields => filterBuffer += ((filter.filter_id, RemoveFieldsFilter(filter.asInstanceOf[RemoveFieldsFilterModel].fields_to_remove)))
        case FilterTypes.GrokFields =>
          val modelInstance = filter.asInstanceOf[GrokFilterModel]
          filterBuffer += ((filter.filter_id, GrokFilter(modelInstance.isPaused.toBoolean, modelInstance.fieldNames, modelInstance.pattern)))
      }
    }*/

    StreamBlueprint(streamId, source, sink, filterBuffer.toMap)
  }


}
