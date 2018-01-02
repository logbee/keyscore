package io.logbee.keyscore.model.filter

case class ExtractToNewFieldFilterModel(filter_type: String, extract_from: String, extract_to: String, regex_rule: String, remove_from: Boolean) extends FilterModel
