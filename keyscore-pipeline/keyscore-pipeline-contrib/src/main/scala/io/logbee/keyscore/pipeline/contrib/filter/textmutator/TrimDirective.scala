package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import io.logbee.keyscore.model.data.{Field, TextValue}
import io.logbee.keyscore.pipeline.api.directive.FieldDirective

case class TrimDirective() extends FieldDirective {
  def invoke(field: Field): Field = {
    field match {
      case Field(name, TextValue(value)) =>
        Field(name, TextValue(value.trim))
      case _ => field
    }
  }
}
