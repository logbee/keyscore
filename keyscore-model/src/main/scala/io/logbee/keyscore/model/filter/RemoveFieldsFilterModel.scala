package io.logbee.keyscore.model.filter

case class RemoveFieldsFilterModel(filter_type: String, fields_to_remove: List[String]) extends FilterModel
