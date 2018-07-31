package io.logbee.keyscore.model.conversion

import com.google.protobuf.Timestamp
import io.logbee.keyscore.model.{DecimalValue, NumberValue, TextValue, TimestampValue}

trait ValueConversion extends TextValueConversion with NumberValueConversion with DecimalValueConversion with TimestampValueConversion

trait TextValueConversion {

  implicit def textValueToString(textValue: TextValue): String = {
    textValue.value
  }

  implicit def textValueFromString(value: String): TextValue = {
    TextValue(value)
  }
}

trait NumberValueConversion {

  implicit def numberValueToInt(numberValue: NumberValue): Int = {
    numberValue.value
  }

  implicit def numberValueFromInt(value: Int): NumberValue = {
    NumberValue(value)
  }
}

trait DecimalValueConversion {

  implicit def decimalValueToDouble(decimalValue: DecimalValue): Double = {
    decimalValue.value
  }

  implicit def doubleToDecimalValue(value: Double): DecimalValue = {
    DecimalValue(value)
  }
}

trait TimestampValueConversion {

  implicit def timestampValueToTimestamp(timestampValue: TimestampValue): Timestamp = {
    Timestamp.newBuilder().setSeconds(timestampValue.seconds).setNanos(timestampValue.nanos).build()
  }

  implicit def timestampValueFromTimestamp(timestamp: Timestamp): TimestampValue = {
    TimestampValue(timestamp.getSeconds, timestamp.getNanos)
  }
}