package streammanagement

import java.util.UUID

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, UniqueKillSwitch}
import io.logbee.keyscore.frontier.filter._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier._
import io.logbee.keyscore.frontier.sources.KafkaSource
import io.logbee.keyscore.frontier.streammanagement.StreamUtils.{createAddFieldsFilter, createExtractFieldsFilter, createExtractToNewFilter, createRemoveFieldsFilter}
import _root_.streammanagement.RunningStreamActor.ShutdownGraph
import _root_.streammanagement.StreamManager._


object StreamManager {
  def props(implicit materializer: ActorMaterializer): Props = Props(new StreamManager)

  case class TranslateAndCreateNewStream(streamModel: StreamModel)

  case class Stream(uuid: UUID,
                    source: Source[CommitableFilterMessage, UniqueKillSwitch],
                    sink: Sink[CommitableFilterMessage, NotUsed],
                    filter: List[Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed]])

  case class ChangeStream(stream: Stream)

  case class CreateNewStream(stream: Stream)

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
          idToActor += stream.uuid -> streamActor
          actorToId += streamActor -> stream.uuid
          sender() ! StreamCreatedWithID(stream.uuid)

      }

    case ChangeStream(stream) =>
      val streamActor = idToActor(stream.uuid)
      actorToId -= streamActor
      idToActor -= stream.uuid
      streamActor ! ShutdownGraph
      val newStreamActor = context.actorOf(RunningStreamActor.props(stream.source, stream.sink, stream.filter))
      idToActor += stream.uuid -> newStreamActor
      actorToId += newStreamActor -> stream.uuid
      sender() ! StreamUpdated(stream.uuid)

    case TranslateAndCreateNewStream(streamModel) =>
      val stream = createStreamFromModel(streamModel)
      self tell(CreateNewStream(stream), sender())
  }

  private def createStreamFromModel(model: StreamModel): StreamManager.Stream = {

    val id = UUID.randomUUID()
    val source = model.source.source_type match {
      case SourceTypes.KafkaSource =>
        val sourceModel = model.source.asInstanceOf[KafkaSourceModel]
        KafkaSource.create(sourceModel.bootstrap_server, sourceModel.source_topic, sourceModel.group_ID, sourceModel.offset_Commit)
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


    StreamManager.Stream(id, source, sink, filterBuffer.toList)
  }

}
