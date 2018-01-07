package io.logbee.keyscore.model.sink

case class KafkaSinkModel(sink_type: String, sink_topic: String, bootstrap_server: String) extends SinkModel
