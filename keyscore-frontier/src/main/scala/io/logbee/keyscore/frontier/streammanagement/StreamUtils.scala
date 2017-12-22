package io.logbee.keyscore.frontier.streammanagement

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import io.logbee.keyscore.frontier.filter._
import io.logbee.keyscore.frontier.sinks.KafkaSink
import io.logbee.keyscore.frontier.sources.KafkaSource
import streammanagement.StreamManager
import org.json4s._
import org.json4s.jackson.JsonMethods._

object StreamUtils {
  implicit val formats = org.json4s.DefaultFormats

  def JsonToStream(stream: String)(implicit actorSystem:ActorSystem): StreamManager.Stream = {

    val streamAsJson = parse(stream).extract[Map[String, Any]]

    val id = streamAsJson("id").asInstanceOf[String]
    val uuid = UUID.fromString(id)

    val sourceMap = streamAsJson("source").asInstanceOf[Map[String, String]]

    val source = KafkaSource.create(sourceMap("bootstrap_server"),
      sourceMap("source_topic"), sourceMap("group_id"), sourceMap("offset_config"))

    val sinkMap = streamAsJson("sink").asInstanceOf[Map[String, String]]

    val sink = KafkaSink.create(sinkMap("sink_topic"), sinkMap("bootstrap_server"))

    val filterStringList = streamAsJson("filter").asInstanceOf[List[List[String]]]

    val filterList = List[Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed]]()

    filterStringList.foreach { filter =>
      filter.head match {
        case "extract_fields" => filterList :+ createExtractFieldsFilter(filter.tail)
        case "add_fields" => filterList :+ createAddFieldsFilter(filter.tail)
        case "remove_fields" => filterList :+ createRemoveFieldsFilter(filter.tail)
        case "extract_to_new"=> filterList :+ createExtractToNewFilter(filter.tail)
      }
    }

    StreamManager.Stream(uuid, source, sink, filterList)

  }

  private def createExtractFieldsFilter(fieldsToExtract: List[String]): Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed] = {
    //takes fieldsToExtract: List[String]
    ExtractFieldsFilter(fieldsToExtract)
  }

  private def createAddFieldsFilter(fieldsToAdd: List[String]): Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed] = {
    //takes fieldsToAdd: Map[String, String]
    val fieldsToAddMap = fieldsToAdd.grouped(2).map { case List(k, v) => k -> v }.toMap
    AddFieldsFilter(fieldsToAddMap)
  }

  private def createRemoveFieldsFilter(fieldsToRemove: List[String]): Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed] = {
    //takes fieldsToRemove: List[String]
    RemoveFieldsFilter(fieldsToRemove)
  }

  private def createExtractToNewFilter(fieldsToRemove: List[String]): Flow[CommitableFilterMessage, CommitableFilterMessage, NotUsed] = {
    //takes extractFrom: String, extractTo: String, regExRule: String, removeFrom: Boolean = false
    val extractFrom = fieldsToRemove(0)
    val extractTo = fieldsToRemove(1)
    val regexRule = fieldsToRemove(2)
    val removeOldField = fieldsToRemove(3).toBoolean
    ExtractToNewFieldFilter(extractFrom, extractTo, regexRule, removeOldField)
  }

}
