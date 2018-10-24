package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.Maturity
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object MaturitySerializer extends CustomSerializer[Maturity](format => ( {
  case JString(maturity) => maturity match {
    case "None" => Maturity.None
    case "Official" => Maturity.Official
    case "Stable" => Maturity.Stable
    case "Development" => Maturity.Development
    case "Experimental" => Maturity.Experimental
  }
  case JNull => Maturity.None
}, {
  case maturity: Maturity =>
    JString(maturity.getClass.getSimpleName.replace("$", ""))
}))
