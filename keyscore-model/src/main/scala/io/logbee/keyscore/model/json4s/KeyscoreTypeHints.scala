package io.logbee.keyscore.model.json4s

import org.json4s.{ShortTypeHints, TypeHints}

object KeyscoreTypeHints {

  val parameterDescriptorHints = ShortTypeHints(List(
//    classOf[BooleanParameterDescriptor],
//    classOf[TextParameterDescriptor],
//    classOf[IntParameterDescriptor],
//    classOf[ListParameterDescriptor],
//    classOf[MapParameterDescriptor]
  ))

  val parameterHints = ShortTypeHints(List(
//    classOf[TextParameter],
//    classOf[BooleanParameter],
//    classOf[IntParameter],
//    classOf[FloatParameter],
//    classOf[TextMapParameter],
//    classOf[TextListParameter]
  ))

  val fieldHints = ShortTypeHints(List(
//    classOf[TextValue],
//    classOf[NumberValue],
//    classOf[DecimalValue],
//    classOf[TimestampValue],
//    classOf[DurationValue]
  ))

  val all: TypeHints = parameterDescriptorHints + parameterHints + fieldHints


}
