package io.logbee.keyscore.model.filter

case class AddFieldsFilterModel(filter_type: String, fields_to_add: Map[String, String]) extends FilterModel
