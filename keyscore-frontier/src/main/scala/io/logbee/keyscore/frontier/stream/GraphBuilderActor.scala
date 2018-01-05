package streammanagement

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import akka.{NotUsed, actor}
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import io.logbee.keyscore.model.StreamModel
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink.{KafkaSinkModel, SinkTypes}
import io.logbee.keyscore.model.source.{KafkaSourceModel, SourceTypes}
import streammanagement.GraphBuilderActor._

import scala.concurrent.Future
import scala.util.Success
import akka.pattern.pipe

object GraphBuilderActor {
  def props()(implicit materializer: ActorMaterializer): Props = actor.Props(new GraphBuilderActor)

  case class BuildGraph(streamId: UUID, stream: StreamModel)

  case class BuiltGraph(graph: RunnableGraph[UniqueKillSwitch])


  case class StreamBlueprint(uuid: UUID,
                             source: Source[CommittableEvent, UniqueKillSwitch],
                             sink: Sink[CommittableEvent, NotUsed],
                             filter: Map[UUID, Flow[CommittableEvent, CommittableEvent, Future[FilterHandle]]])

  case class UpdateFilter(uuid:UUID,configuration:GrokFilterConfiguration)

  case class FilterUpdated(id: UUID)

  case class FilterNotFound(id: UUID)

  case object PrintList

}

class GraphBuilderActor(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

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

    case PrintList =>
      println("--------------------------filterHandles: " + filterHandles.size)
      for((k,v) <- filterHandles) printf("id: %s, value: %s\n",k.toString,v.toString)

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

    val source = model.source.source_type match {
      case SourceTypes.KafkaSource =>
        val sourceModel = model.source.asInstanceOf[KafkaSourceModel]
        KafkaSource.create(sourceModel.bootstrap_server, sourceModel.source_topic, sourceModel.group_ID, sourceModel.offset_commit)
    }
    val sink = model.sink.sink_type match {
      case SinkTypes.KafkaSink =>
        val sinkModel = model.sink.asInstanceOf[KafkaSinkModel]
        KafkaSink.create(sinkModel.sink_topic, sinkModel.bootstrap_server)
    }

    val filterBuffer = scala.collection.mutable.Map[UUID, Flow[CommittableEvent, CommittableEvent, Future[FilterHandle]]]()

    model.filter.foreach { filter =>
      filter.filter_type match {
        case FilterTypes.ExtractFields => filterBuffer += ((filter.filter_id, RetainFieldsFilter(filter.asInstanceOf[RetainFieldsFilterModel].fields_to_extract)))
        case FilterTypes.AddFields => filterBuffer += ((filter.filter_id, AddFieldsFilter(filter.asInstanceOf[AddFieldsFilterModel].fields_to_add)))
        case FilterTypes.RemoveFields => filterBuffer += ((filter.filter_id, RemoveFieldsFilter(filter.asInstanceOf[RemoveFieldsFilterModel].fields_to_remove)))
        case FilterTypes.GrokFields =>
          val modelInstance = filter.asInstanceOf[GrokFilterModel]
          filterBuffer += ((filter.filter_id, GrokFilter(modelInstance.isPaused.toBoolean, modelInstance.grokFields, modelInstance.pattern)))
      }
    }

    StreamBlueprint(streamId, source, sink, filterBuffer.toMap)
  }
}


case class StreamHandle(killSwitch: UniqueKillSwitch, filterHandles: Map[UUID, FilterHandle])