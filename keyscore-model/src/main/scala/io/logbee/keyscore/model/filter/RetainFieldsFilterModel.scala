package io.logbee.keyscore.model.filter

case class RetainFieldsFilterModel(filter_type: String, fields_to_extract: List[String]) extends FilterModel
