package io.logbee.keyscore.model.filter

case class GrokFilterModel(filter_type: String, isPaused: String, grokFields: List[String], pattern: String) extends FilterModel