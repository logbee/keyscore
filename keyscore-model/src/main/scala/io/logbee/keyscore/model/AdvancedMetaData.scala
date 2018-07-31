package io.logbee.keyscore.model

import com.google.protobuf.timestamp.Timestamp

case class AdvancedMetaData(metaData: MetaData) {

  def findLabel(name: String): Option[AdvancedLabel[_]] = {
    metaData.labels.find(_.name == name).map(AdvancedLabel(_))
  }

  def findTextLabel(name: String): Option[AdvancedLabel[String]] = {
    metaData.labels.find {
      case Label(`name`, TextValue(_)) => true
      case _ => false
    }.map(AdvancedLabel[String])
  }
}

case class AdvancedLabel[T](label: Label) {
  def name: String = label.name
  def value: T = {
    val value = label.value match {
      case text: TextValue => text.value
      case number: NumberValue => number.value
      case decimal: DecimalValue => decimal.value
      case timestamp: TimestampValue => Timestamp(timestamp.seconds, timestamp.nanos)
      case _ => throw new IllegalStateException()
    }

    value.asInstanceOf[T]
  }
}
