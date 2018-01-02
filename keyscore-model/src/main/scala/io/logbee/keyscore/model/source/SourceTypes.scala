package io.logbee.keyscore.model.source

/** Constants to match Source names in JSON-Objects */
object SourceTypes {
  type SourceTypes = String
  /** Name of the SourceType field in JSON */
  val SourceType = "source_type"

  val KafkaSource = "kafka_source"

}
