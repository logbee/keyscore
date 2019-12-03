package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import io.logbee.keyscore.model.data.{Field, TextValue}
import io.logbee.keyscore.pipeline.api.directive.FieldDirective

case class FindReplaceDirective(find: String, replace: String) extends FieldDirective {
  def invoke(field: Field): Seq[Field] = {
     field match{
       case Field(name, TextValue(value, _)) =>
         Seq(Field(name,TextValue(value.replace(find,replace))))
       case _ => Seq(field)
     }
  }
}
