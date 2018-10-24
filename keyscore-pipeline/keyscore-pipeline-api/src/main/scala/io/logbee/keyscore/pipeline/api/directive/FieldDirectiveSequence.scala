package io.logbee.keyscore.pipeline.api.directive

object FieldDirectiveSequence {
  def apply[D <: FieldDirective](fieldName: String, directives: Seq[D]): FieldDirectiveSequence[D] =
    new FieldDirectiveSequence(fieldName, directives)
}

class FieldDirectiveSequence[D <: FieldDirective](val fieldName: String, val directives: Seq[D]) {

}
