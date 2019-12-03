package io.logbee.keyscore.pipeline.api.directive

import io.logbee.keyscore.model.data.Field

trait FieldDirective {

  def invoke(field: Field): Seq[Field]
}
