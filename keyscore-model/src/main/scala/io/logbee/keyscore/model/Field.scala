package io.logbee.keyscore.model

trait Field {
  val name: String
  val kind: String
}

case class TextField(name: String, value: String) extends Field {
  override val kind: String = "text"
}

case class NumberField(name: String, value: BigDecimal) extends Field {
  override val kind: String = "number"
}

case class TimestampField(name: String, value: Long) extends Field {
  override val kind: String = "timestamp"
}
