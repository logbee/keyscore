package io.logbee.keyscore.model

trait Field[+T] {
  val name: String
  val kind: String
  val value: T
}

case class TextField(name: String, value: String) extends Field[String] {
  override val kind: String = "text"
}

case class NumberField(name: String, value: BigDecimal) extends Field[BigDecimal] {
  override val kind: String = "number"
}

case class TimestampField(name: String, value: Long) extends Field[Long] {
  override val kind: String = "timestamp"
}
