package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.filter._
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object FilterStatusSerializer extends CustomSerializer[FilterStatus](format => ( {
  case JString(filter) => filter match {
    case "Unknown" => Unknown
    case "Paused" => Paused
    case "Running" => Running
    case "Drained" => Drained
  }
  case JNull => null
}, {
  case filter: FilterStatus => JString(filter.getClass.getSimpleName.replace("$", ""))
}))