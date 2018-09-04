package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.IconFormat
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object IconFormatSerializer extends CustomSerializer[IconFormat](format => ( {
  case JString(format) => format match {
    case "SVG" => IconFormat.SVG
  }
  case JNull => IconFormat.SVG
}, {
  case format: IconFormat =>
    JString(format.getClass.getSimpleName.replace("$", ""))
}))
