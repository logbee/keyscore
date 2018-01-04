package io.logbee.keyscore.model.filter

/** Constants to match Filter names in JSON-Objects */
object FilterTypes {
  type FilterTypes = String
  /** Name of the FilterType field in JSON */
  val FilterType = "filter_type"
  val ExtractFields = "extract_fields"
  val AddFields = "add_fields"
  val RemoveFields = "remove_fields"
}
