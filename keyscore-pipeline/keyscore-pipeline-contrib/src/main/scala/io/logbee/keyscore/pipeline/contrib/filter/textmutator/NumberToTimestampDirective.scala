package io.logbee.keyscore.pipeline.contrib.filter.textmutator

import java.time._

import io.logbee.keyscore.model.data.{Field, NumberValue, TimestampValue}
import io.logbee.keyscore.pipeline.api.directive.FieldDirective
import io.logbee.keyscore.pipeline.contrib.filter.textmutator.NumberToTimestampDirective.{Millis, Seconds}

object NumberToTimestampDirective {
  sealed trait Unit
  case object Seconds extends Unit
  case object Millis extends Unit
}

case class NumberToTimestampDirective(unit: NumberToTimestampDirective.Unit, sourceTimeZone: Option[ZoneId] = None) extends FieldDirective {

  def invoke(field: Field): Seq[Field] = {

    field match {

      case Field(name, NumberValue(value)) =>

        val instant = unit match {
          case Seconds => Instant.ofEpochSecond(value)
          case Millis => Instant.ofEpochMilli(value)
          case _ => Instant.ofEpochSecond(value)
        }

        Seq(Field(name, TimestampValue(instant.getEpochSecond, instant.getNano)))

      case _ => Seq(field)
    }
  }
}
