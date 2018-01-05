package io.logbee.keyscore.model.filter

import java.util.UUID

case class AddFieldsFilterModel(filter_id:UUID,filter_type: String, fields_to_add: Map[String, String]) extends FilterModel
