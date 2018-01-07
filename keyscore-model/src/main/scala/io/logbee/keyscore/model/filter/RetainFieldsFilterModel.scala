package io.logbee.keyscore.model.filter

import java.util.UUID

case class RetainFieldsFilterModel(filter_id: UUID, filter_type: String, fields_to_extract: List[String]) extends FilterModel
