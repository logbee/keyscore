package io.logbee.keyscore.frontier

case class StreamModel(id: String, source: SourceModel, sink: SinkModel, filter: List[FilterModel])

trait SourceModel {
  def source_type: String
}

case class KafkaSourceModel(source_type: String, bootstrap_server: String, source_topic: String, group_ID: String, offset_commit: String) extends SourceModel

/** Constants to match Source names in JSON-Objects */
object SourceTypes {
  type SourceTypes = String
  /** Name of the SourceType field in JSON */
  val SourceType = "source_type"

  val KafkaSource = "kafka_source"

}


trait SinkModel {
  def sink_type: String
}

case class KafkaSinkModel(sink_type: String, sink_topic: String, bootstrap_server: String) extends SinkModel

/** Constants to match Sink names in JSON-Objects */
object SinkTypes {
  type SinkTypes = String
  /** Name of the SinkType field in JSON */
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

/** Constants to match Filter names in JSON-Objects */
object FilterTypes {
  type FilterTypes = String
  /** Name of the FilterType field in JSON */
  val FilterType = "filter_type"

  val ExtractFields = "extract_fields"
  val AddFields = "add_fields"
  val RemoveFields = "remove_fields"
  val ExtractToNew = "extract_to_new"
}