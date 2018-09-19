package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.data.Health
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

class HealthValueSerializer extends CustomSerializer[Health](format => ({
  case JString(fieldValueType) => fieldValueType match {
    case "Gray" => Health.Gray
    case "Green" => Health.Green
    case "Yellow" => Health.Yellow
    case "Red" => Health.Red
  }
  case JNull => Health.Gray
}, {
  case health: Health =>
    JString(health.getClass.getSimpleName.replace("$", ""))
}))