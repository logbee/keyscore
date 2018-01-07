package io.logbee.keyscore.model.filter

import java.util.UUID

case class RemoveFieldsFilterModel(filter_id:UUID,filter_type: String, fields_to_remove: List[String]) extends FilterModel
