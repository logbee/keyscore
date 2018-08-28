package io.logbee.keyscore.model.data

import com.google.protobuf.Timestamp
import io.logbee.keyscore.model.conversion._

object TextField extends TextFieldConversion
case class TextField(name: String, value: String)

object NumberField extends NumberFieldConversion
case class NumberField(name: String, value: Long)

object DecimalField extends DecimalFieldConversion
case class DecimalField(name: String, value: Double)

object TimestampField extends TimestampFieldConversion {
  def apply(name: String, timestamp: Timestamp): TimestampField = new TimestampField(name, timestamp.getSeconds, timestamp.getNanos)
  def apply(name: String, value: TimestampValue): TimestampField = new TimestampField(name, value.seconds, value.nanos)
}
case class TimestampField(name: String, seconds: Long, nanos: Int = 0)
