package io.logbee.keyscore.model.data

trait RecordCompanion {

  def apply(fields: Field*): Record = Record(fields.toList)
}
