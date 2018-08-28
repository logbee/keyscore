package io.logbee.keyscore.model.conversion

import com.google.protobuf.Timestamp
import io.logbee.keyscore.model.data._

case class BaseField(private val field: Field) {
  def isTextField: Boolean = field.value.isInstanceOf[TextValue]
  def isNumberField: Boolean = field.value.isInstanceOf[NumberValue]
  def isDecimalField: Boolean = field.value.isInstanceOf[DecimalValue]
  def isTimestampField: Boolean = field.value.isInstanceOf[TimestampValue]
  def toTextField: TextField = TextField(field.name, field.value.asInstanceOf[TextValue].value)
  def toNumberField: NumberField = NumberField(field.name, field.value.asInstanceOf[NumberValue].value)
  def toDecimalField: DecimalField = DecimalField(field.name, field.value.asInstanceOf[DecimalValue].value)
  def toTimestampField: TimestampField = TimestampField(field.name, field.value.asInstanceOf[TimestampValue])
}

trait FieldConversions {

  implicit def toBaseField(field: Field): BaseField = BaseField(field)
}

trait TextFieldConversion {
  implicit def textFieldToField(field: TextField): Field = Field(field.name, TextValue(field.value))
  implicit def fieldToTextField(field: Field): TextField = TextField(field.name, field.value.asInstanceOf[TextValue].value)
}

trait NumberFieldConversion {
  implicit def numberFieldToField(field: NumberField): Field = Field(field.name, NumberValue(field.value))
  implicit def fieldToNumberField(field: Field): NumberField = NumberField(field.name, field.value.asInstanceOf[NumberValue].value)
}

trait DecimalFieldConversion {
  implicit def decimalFieldToField(field: DecimalField): Field = Field(field.name, DecimalValue(field.value))
  implicit def fieldToDecimalField(field: Field): DecimalField = DecimalField(field.name, field.value.asInstanceOf[DecimalValue].value)
}

trait TimestampFieldConversion {
  implicit def timestampFieldToField(field: TimestampField): Field = Field(field.name, TimestampValue(field.seconds, field.nanos))
  implicit def fieldToTimestampField(field: Field): TimestampField = TimestampField(field.name, field.value.asInstanceOf[Timestamp])
}