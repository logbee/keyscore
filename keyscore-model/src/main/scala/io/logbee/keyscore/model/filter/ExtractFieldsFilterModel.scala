package io.logbee.keyscore.model.filter

case class ExtractFieldsFilterModel(filter_type: String, fields_to_extract: List[String]) extends FilterModel
