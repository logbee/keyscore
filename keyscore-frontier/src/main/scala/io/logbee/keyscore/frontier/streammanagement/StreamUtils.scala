package io.logbee.keyscore.frontier.streammanagement

import akka.NotUsed
import akka.stream.scaladsl.Flow
import filter.{CommitableFilterMessage, ExtractFieldsFilter}
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import streammanagement.StreamManager
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization

object StreamUtils {
  implicit val formats = org.json4s.DefaultFormats

  def JsonToStream(stream: String): StreamManager.Stream = {

    val streamAsJson = parse(stream).extract[Map[String, Any]]

    val sourceMap = streamAsJson("source").asInstanceOf[Map[String, String]]

    val source = KafkaSource.create(sourceMap("bootstrap_server"),
      sourceMap("source_topic"), sourceMap("group_id"), sourceMap("offset_config"))

    val sinkMap = streamAsJson("sink").asInstanceOf[Map[String, String]]

    val sink = KafkaSink.create(sinkMap("sink_topic"), sinkMap("bootstrap_server"))

    val filterStringList = streamAsJson("filter").asInstanceOf[List[List[String]]]

    var filterList = List[Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed]]()

    filterStringList.foreach { filter =>
      filter.head match {
        case "extract_field" => filterList :: createExtractFieldFilter(filter.tail)

      }
    }


  }

  private def createExtractFieldFilter(fieldsToExtract: List[String]): Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed] = {
    ExtractFieldsFilter(fieldsToExtract)
  }
}
