package io.logbee.keyscore.model.sink

/** Constants to match Sink names in JSON-Objects */
object SinkTypes {
  type SinkTypes = String
  /** Name of the SinkType field in JSON */
  val SinkType = "sink_type"

  val KafkaSink = "kafka_sink"
}
