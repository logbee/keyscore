package io.logbee.keyscore.frontier

case class StreamModel(id: String, source: SourceModel, sink: SinkModel, filter: List[FilterModel])

trait SourceModel {
  def source_type: String
}

case class KafkaSourceModel(source_type: String, bootstrap_server: String, source_topic: String, group_ID: String, offset_Commit: String) extends SourceModel


object SourceTypes {
  type SourceTypes = String
  val SourceType = "source_type"
  val KafkaSource = "kafka_source"

}


trait SinkModel {
  def sink_type: String
}

case class KafkaSinkModel(sink_type: String, sink_topic: String, bootstrap_server: String) extends SinkModel

object SinkTypes {
  type SinkTypes = String
  val SinkType = "sink_type"
  val KafkaSink = "kafka_sink"
}

trait FilterModel {
  def filter_type: String
}

case class ExtractFieldsFilterModel(filter_type: String, fields_to_extract: List[String]) extends FilterModel

case class AddFieldsFilterModel(filter_type: String, fields_to_add: Map[String, String]) extends FilterModel

case class RemoveFieldsFilterModel(filter_type: String, fields_to_remove: List[String]) extends FilterModel

case class ExtractToNewFieldFilterModel(filter_type: String, extract_from: String, extract_to: String, regex_rule: String, remove_from: Boolean) extends FilterModel

object FilterTypes {
  type FilterTypes = String
  val FilterType = "filter_type"
  val ExtractFields = "extract_fields"
  val AddFields = "add_fields"
  val RemoveFields = "remove_fields"
  val ExtractToNew = "extract_to_new"
}