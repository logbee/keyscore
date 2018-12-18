package io.logbee.keyscore.pipeline.api.directive

import io.logbee.keyscore.model.configuration.ParameterSet

//object FieldDirectiveSequence {
//  def apply[D <: FieldDirective](fieldName: String, parameters: ParameterSet, directives: Seq[D]): FieldDirectiveSequence[D] =
//    new FieldDirectiveSequence(fieldName, parameters, directives)
//}

case class FieldDirectiveSequence[D <: FieldDirective](fieldName: String, parameters: ParameterSet, directives: Seq[D])
