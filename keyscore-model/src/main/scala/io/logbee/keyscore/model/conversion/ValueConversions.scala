package io.logbee.keyscore.model.conversion

import com.google.protobuf.{ByteString, Duration, Timestamp}
import io.logbee.keyscore.model.data._

import scala.language.implicitConversions

trait ValueConversion extends TextValueConversion with NumberValueConversion with DecimalValueConversion with TimestampValueConversion


trait BooleanValueConversion {
  implicit def booleanValueToBoolean(booleanValue: BooleanValue): Boolean = booleanValue.value
  implicit def booleanValueFromString(value: Boolean): BooleanValue = BooleanValue(value)
}

trait TextValueConversion {
  implicit def textValueToString(textValue: TextValue): String = textValue.value
  implicit def textValueFromString(value: String): TextValue = TextValue(value)
}

trait NumberValueConversion {
  implicit def numberValueToInt(numberValue: NumberValue): Long = numberValue.value
  implicit def numberValueFromInt(value: Long): NumberValue = NumberValue(value)
}

trait DecimalValueConversion {
  implicit def decimalValueToDouble(decimalValue: DecimalValue): Double = decimalValue.value
  implicit def doubleToDecimalValue(value: Double): DecimalValue = DecimalValue(value)
}

trait TimestampValueConversion {

  def apply(timestamp: Timestamp) = new TimestampValue(timestamp.getSeconds, timestamp.getNanos)

  implicit def timestampValueToTimestamp(timestampValue: TimestampValue): Timestamp = {
    Timestamp.newBuilder().setSeconds(timestampValue.seconds).setNanos(timestampValue.nanos).build()
  }

  implicit def timestampValueFromTimestamp(timestamp: Timestamp): TimestampValue = {
    TimestampValue(timestamp)
  }
}

trait DurationValueConversion {

  def apply(duration: Duration) = new DurationValue(duration.getSeconds, duration.getNanos)

  implicit def durationValueToTimestamp(durationValue: DurationValue): Duration = {
    Duration.newBuilder().setSeconds(durationValue.seconds).setNanos(durationValue.nanos).build()
  }

  implicit def durationValueFromDuration(duration: Duration): DurationValue = {
    DurationValue(duration)
  }
}

trait BinaryValueConversion {

}
