package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time._

import io.logbee.keyscore.model.data.{Field, TextValue, TimestampValue}
import io.logbee.keyscore.pipeline.api.directive.FieldDirective

case class ToTimestampDirective(pattern: String) extends FieldDirective {
  def invoke(field: Field): Field = {
    val format = DateTimeFormatter.ofPattern(pattern)
    field match {
      case Field(name, TextValue(value)) =>
        var date: Instant = null
        try {
          if (value.contains("T") && value.contains("+")) {
            date = OffsetDateTime.parse(value, format).toInstant
          } else if (value.contains("T")) {
            date = LocalDateTime.parse(value, format).toInstant(ZoneOffset.UTC)
          } else {
            try {
              date = LocalDateTime.parse(value, format).toInstant(ZoneOffset.UTC)
            }
            catch {
              case _: DateTimeParseException =>
                date = LocalDate.parse(value, format).atStartOfDay(ZoneOffset.UTC).toInstant
            }
          }
        } catch {
          case _: DateTimeParseException =>
            return field
        }

        if (date != null) {
          val seconds = date.getEpochSecond
          val nanos = date.getNano
          return Field(name, TimestampValue(seconds, nanos))
        }
        else {
          field
        }
      case _ =>
        field
    }
  }
}
