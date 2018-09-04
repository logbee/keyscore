package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.ExpressionType
import io.logbee.keyscore.model.descriptor.ExpressionType.{Glob, Grok, JSONPath, RegEx}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object PatternTypeSerializer extends CustomSerializer[ExpressionType](format => ( {
  case JString(patternType) => patternType match {
    case "RegEx" => RegEx
    case "Grok" => Grok
    case "Glob" => Glob
    case "JSONPath" => JSONPath
  }
  case JNull => RegEx
}, {
  case expressionType: ExpressionType => JString(expressionType.getClass.getSimpleName.replace("$", ""))
}))