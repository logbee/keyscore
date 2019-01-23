package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

import io.logbee.keyscore.model.data.{Field, TextValue, TimestampValue}
import io.logbee.keyscore.pipeline.api.directive.FieldDirective

case class ToTimestampDirective(pattern: String) extends FieldDirective {
  def invoke(field: Field): Field = {
    val format = DateTimeFormatter.ofPattern(pattern)
    field match {
      case Field(name, TextValue(value)) =>
        val date = LocalDateTime.parse(value,format).toInstant(ZoneOffset.UTC)
        val seconds = date.getEpochSecond()
        val nanos = date.getNano()
        Field(name, TimestampValue(seconds, nanos))
      case _ =>
        field
    }
  }
}
