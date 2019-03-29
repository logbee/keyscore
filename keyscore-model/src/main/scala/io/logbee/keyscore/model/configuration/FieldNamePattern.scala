package io.logbee.keyscore.model.configuration

import io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor.PatternType

object FieldNamePattern {
  def apply(pattern: String, patternType: PatternType): FieldNamePattern = new FieldNamePattern(pattern, patternType)
}

class FieldNamePattern(val pattern: String, val patternType: PatternType) {

  def matches(name: String): Boolean = patternType match {
    case PatternType.RegEx => pattern.r.pattern.matcher(name).matches()
    case PatternType.None => pattern.equals(name)
    case _ => false
  }
}
