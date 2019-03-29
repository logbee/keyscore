package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import io.logbee.keyscore.model.configuration.FieldNamePattern
import io.logbee.keyscore.pipeline.api.directive.FieldDirective

case class TextMutatorDirectiveSequence[D <: FieldDirective](fieldNamePattern: FieldNamePattern, inplace: Boolean, mutatedFieldName: String, directives: Seq[D])
