package io.logbee.keyscore.model.filter

import java.util.UUID

trait FilterModel {
  def filter_type: String
  def filter_id:UUID
}
