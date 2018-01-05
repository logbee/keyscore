package io.logbee.keyscore.model.filter

case class GrokFilterModel(filter_type: String, isPaused: String, grok_fields: List[String], pattern: String) extends FilterModel