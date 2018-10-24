package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import io.logbee.keyscore.model.data.Field
import io.logbee.keyscore.pipeline.api.directive.FieldDirective

case class FindReplaceDirective(find: String, replace: String) extends FieldDirective {
  def invoke(field: Field): Field = {
    field
  }
}
