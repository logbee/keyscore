package io.logbee.keyscore.model

trait RecordCompanion {

  def apply(fields: Field*): Record = Record(fields.toList)
}
