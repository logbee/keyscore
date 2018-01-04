package io.logbee.keyscore.model

trait Field {
  def name: String
}

case class TextField(name: String, value: String) extends Field

case class NumberField(name: String, value: BigDecimal) extends Field

case class TimestampField(name: String, value: Long) extends Field
