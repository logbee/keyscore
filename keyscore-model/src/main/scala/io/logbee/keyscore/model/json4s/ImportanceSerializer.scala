package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.data.Importance
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object ImportanceSerializer extends CustomSerializer[Importance]( format => ({
  case JString(importance) => importance match {
    case "Undefined" => Importance.Undefined
    case "Low" => Importance.Low
    case "Lower" => Importance.Lower
    case "Medium" => Importance.Medium
    case "Higher" => Importance.Higher
    case "High" => Importance.High
  }
  case JNull => Importance.Undefined
}, {
  case importance: Importance =>
    JString(importance.getClass.getSimpleName.replace("$", ""))
}))
