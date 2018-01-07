package io.logbee.keyscore.model.source

case class KafkaSourceModel(source_type: String, bootstrap_server: String, source_topic: String, group_ID: String, offset_commit: String) extends SourceModel
