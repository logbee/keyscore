package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor.PatternType
import io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor.PatternType.{Glob, ExactMatch, RegEx}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object PatternTypeSerializer extends CustomSerializer[PatternType](format => ( {
  case JString(patternType) => patternType match {
    case "RegEx" => RegEx
    case "Glob" => Glob
    case _ => ExactMatch
  }
  case JNull => ExactMatch
}, {
  case patternType: PatternType => JString(patternType.getClass.getName.split("\\$").last)

}))
