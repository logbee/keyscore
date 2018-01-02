package io.logbee.keyscore.frontier.stream

import java.util.UUID

import _root_.streammanagement.RunningStreamActor.ShutdownGraph
import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import io.logbee.keyscore.frontier.filter._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import io.logbee.keyscore.frontier.stream.StreamManager._
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.sink.{KafkaSinkModel, SinkTypes}
import io.logbee.keyscore.model.source.{KafkaSourceModel, SourceTypes}
import streammanagement.RunningStreamActor


object StreamManager {
  def props(implicit materializer: ActorMaterializer): Props = Props(new StreamManager)

  case class TranslateAndCreateNewStream(streamModel: StreamModel)

  case class StreamInstance(uuid: UUID,
                            source: Source[CommitableFilterMessage, UniqueKillSwitch],
                            sink: Sink[CommitableFilterMessage, NotUsed],
                            filter: List[Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed]])

  case class ChangeStream(stream: StreamInstance)

  case class CreateNewStream(stream: StreamInstance)

  case class StreamCreatedWithID(id: UUID)

  case class StreamUpdated(id: UUID)

}

class StreamManager(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
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
          self tell(ChangeStream(stream), sender())
        case None =>
          val streamActor = context.actorOf(RunningStreamActor.props(stream.source, stream.sink, stream.filter))
          addStreamActor(stream, streamActor)
          sender() ! StreamCreatedWithID(stream.uuid)
      }

    case ChangeStream(stream) =>
      val streamActor: ActorRef = removeStreamActor(stream)
      streamActor ! ShutdownGraph
      val newStreamActor = context.actorOf(RunningStreamActor.props(stream.source, stream.sink, stream.filter))
      addStreamActor(stream, newStreamActor)
      sender() ! StreamUpdated(stream.uuid)

    case TranslateAndCreateNewStream(streamModel) =>
      val stream = createStreamFromModel(streamModel)
      self tell(CreateNewStream(stream), sender())
  }

  private def addStreamActor(stream: StreamInstance, actor: ActorRef): Unit = {
    idToActor += stream.uuid -> actor
    actorToId += actor -> stream.uuid
  }

  private def removeStreamActor(stream: StreamInstance): ActorRef = {
    val streamActor = idToActor(stream.uuid)
    actorToId -= streamActor
    idToActor -= stream.uuid
    streamActor
  }

  private def createStreamFromModel(model: StreamModel): StreamManager.StreamInstance = {

    val id = UUID.randomUUID()
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

    val filterBuffer = scala.collection.mutable.ListBuffer[Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed]]()

    model.filter.foreach { filter =>
      filter.filter_type match {
        case FilterTypes.ExtractFields => filterBuffer.append(ExtractFieldsFilter(filter.asInstanceOf[ExtractFieldsFilterModel].fields_to_extract))
        case FilterTypes.AddFields => filterBuffer.append(AddFieldsFilter(filter.asInstanceOf[AddFieldsFilterModel].fields_to_add))
        case FilterTypes.RemoveFields => filterBuffer.append(RemoveFieldsFilter(filter.asInstanceOf[RemoveFieldsFilterModel].fields_to_remove))
        case FilterTypes.ExtractToNew => val filterModel = filter.asInstanceOf[ExtractToNewFieldFilterModel]
          filterBuffer.append(ExtractToNewFieldFilter(filterModel.extract_from, filterModel.extract_to, filterModel.regex_rule, filterModel.remove_from))
      }
    }


    StreamManager.StreamInstance(id, source, sink, filterBuffer.toList)
  }

}
