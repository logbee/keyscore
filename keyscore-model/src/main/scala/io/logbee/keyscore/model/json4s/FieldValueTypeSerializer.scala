package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.data.FieldValueType
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object FieldValueTypeSerializer extends CustomSerializer[FieldValueType](format => ({
  case JString(fieldValueType) => fieldValueType match {
    case "Unknown" => FieldValueType.Unknown
    case "Boolean" => FieldValueType.Boolean
    case "Number" => FieldValueType.Number
    case "Decimal" => FieldValueType.Decimal
    case "Text" => FieldValueType.Text
    case "Timestamp" => FieldValueType.Timestamp
    case "Duration" => FieldValueType.Duration
  }
  case JNull => FieldValueType.Unknown
}, {
  case fieldValueType: FieldValueType =>
    JString(fieldValueType.getClass.getSimpleName.replace("$", ""))
}))