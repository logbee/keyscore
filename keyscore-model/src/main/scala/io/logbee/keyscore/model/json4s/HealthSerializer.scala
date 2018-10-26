package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.data.Health
import io.logbee.keyscore.model.data.Health.{Green, Red, Yellow}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}


case object HealthSerializer extends CustomSerializer[Health](format => ( {
  case JString(health) => health match {
    case "Green" => Green
    case "Yellow" => Yellow
    case "Red" => Red
  }
  case JNull => null
}, {
  case health: Health => JString(health.getClass.getSimpleName.replace("$", ""))
}))