package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.configuration.{Configuration, Parameter}
import io.logbee.keyscore.model.descriptor._
import org.json4s.{FullTypeHints, TypeHints}

object KeyscoreTypeHints {

  val hints = FullTypeHints(List(
    classOf[Configuration],
    classOf[Descriptor],
    classOf[FilterDescriptor],
    classOf[SinkDescriptor],
    classOf[SourceDescriptor],
    classOf[Parameter],
    classOf[ParameterDescriptor],
    classOf[ParameterGroupCondition]
  ))


  val all: TypeHints = hints


}
