package io.logbee.keyscore.model.filter

import java.util.UUID

case class GrokFilterModel(filter_id:UUID,filter_type: String, isPaused: String, fieldNames: List[String], pattern: String) extends FilterModel