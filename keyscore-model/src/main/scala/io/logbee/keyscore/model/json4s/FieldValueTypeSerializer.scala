package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.data.FieldValueType
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object FieldValueTypeSerializer extends CustomSerializer[FieldValueType](format => ({
  case JString(fieldValueType) => fieldValueType match {
    case "Unknown" => FieldValueType.Unknown
    case "BooleanValue" => FieldValueType.Boolean
    case "NumberValue" => FieldValueType.Number
    case "DecimalValue" => FieldValueType.Decimal
    case "TextValue" => FieldValueType.Text
    case "TimestampValue" => FieldValueType.Timestamp
    case "DurationValue" => FieldValueType.Duration
  }
  case JNull => FieldValueType.Unknown
}, {
  case fieldValueType: FieldValueType =>
    JString(fieldValueType.getClass.getSimpleName.replace("$", ""))
}))