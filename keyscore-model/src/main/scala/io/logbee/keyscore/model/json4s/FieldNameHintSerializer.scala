package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.FieldNameHint
import io.logbee.keyscore.model.descriptor.FieldNameHint.{AbsentField, AnyField, PresentField}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object FieldNameHintSerializer extends CustomSerializer[FieldNameHint](format => ( {
  case JString(hint) => hint match {
    case "AnyField" => AnyField
    case "PresentField" => PresentField
    case "AbsentField" => AbsentField
  }
  case JNull => AnyField
}, {
  case hint: FieldNameHint =>
    JString(hint.getClass.getSimpleName.replace("$", ""))
}))