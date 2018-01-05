package io.logbee.keyscore.frontier.stream

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import io.logbee.keyscore.frontier.filters._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import org.json4s._
import org.json4s.jackson.JsonMethods._

object StreamUtils {
  implicit val formats = org.json4s.DefaultFormats

  def JsonToStream(stream: String)(implicit actorSystem:ActorSystem): StreamManager.StreamInstance = {

    val streamAsJson = parse(stream).extract[Map[String, Any]]

    val id = streamAsJson("id").asInstanceOf[String]
    val uuid = UUID.fromString(id)

    val sourceMap = streamAsJson("source").asInstanceOf[Map[String, String]]

    val source = KafkaSource.create(sourceMap("bootstrap_server"),
      sourceMap("source_topic"), sourceMap("group_id"), sourceMap("offset_config"))

    val sinkMap = streamAsJson("sink").asInstanceOf[Map[String, String]]

    val sink = KafkaSink.create(sinkMap("sink_topic"), sinkMap("bootstrap_server"))

    val filterStringList = streamAsJson("filter").asInstanceOf[List[List[String]]]

    val filterList = List[Flow[CommittableEvent, CommittableEvent, NotUsed]]()

    filterStringList.foreach { filter =>
      filter.head match {
        case "extract_fields" => filterList :+ createExtractFieldsFilter(filter.tail)
        case "add_fields" => filterList :+ createAddFieldsFilter(filter.tail)
        case "remove_fields" => filterList :+ createRemoveFieldsFilter(filter.tail)
        case "grok_fields" => filterList :+ createGrokFields(filter.tail)
      }
    }

    StreamManager.StreamInstance(uuid, source, sink, filterList)

  }

  private def createExtractFieldsFilter(fieldsToExtract: List[String]): Flow[CommittableEvent, CommittableEvent, NotUsed] = {
    //takes fieldsToExtract: List[String]
    RetainFieldsFilter(fieldsToExtract)
  }

  private def createAddFieldsFilter(fieldsToAdd: List[String]): Flow[CommittableEvent, CommittableEvent, NotUsed] = {
    //takes fieldsToAdd: Map[String, String]
    val fieldsToAddMap = fieldsToAdd.grouped(2).map { case List(k, v) => k -> v }.toMap
    AddFieldsFilter(fieldsToAddMap)
  }

  private def createRemoveFieldsFilter(fieldsToRemove: List[String]): Flow[CommittableEvent, CommittableEvent, NotUsed] = {
    //takes fieldsToRemove: List[String]
    RemoveFieldsFilter(fieldsToRemove)
  }

  private def createGrokFields(grokFields: List[String]): Flow[CommittableEvent, CommittableEvent, NotUsed] = {
    //extracts fields with regex
    val isPaused = grokFields(0).toBoolean
    val fieldNamesAsString = grokFields(1).split("|").toList
    val pattern = grokFields(3)
    val config = GrokFilterConfiguration(Option(isPaused), Option(fieldNamesAsString), Option(pattern))

    GrokFilter(config)
  }


}
