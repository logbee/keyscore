package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.{NumberField, TextField, TimestampField}
import io.logbee.keyscore.model.filter._
import org.json4s.{ShortTypeHints, TypeHints}

object KeyscoreTypeHints {

  val parameterDescriptorHints = ShortTypeHints(List(
    classOf[BooleanParameterDescriptor],
    classOf[TextParameterDescriptor],
    classOf[IntParameterDescriptor],
    classOf[ListParameterDescriptor],
    classOf[MapParameterDescriptor]
  ))

  val parameterHints = ShortTypeHints(List(
    classOf[TextParameter],
    classOf[BooleanParameter],
    classOf[IntParameter],
    classOf[FloatParameter],
    classOf[TextMapParameter],
    classOf[TextListParameter]
  ))

  val fieldHints = ShortTypeHints(List(
    classOf[TextField],
    classOf[NumberField],
    classOf[TimestampField]
  ))

  val all: TypeHints = parameterDescriptorHints + parameterHints + fieldHints


}
