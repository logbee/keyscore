package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.{Descriptor, FilterDescriptor, ParameterDescriptor, ParameterGroupCondition}
import org.json4s.{FullTypeHints, TypeHints}

object KeyscoreTypeHints {

  val hints = FullTypeHints(List(
    classOf[Descriptor],
    classOf[FilterDescriptor],
    classOf[ParameterDescriptor],
    classOf[ParameterGroupCondition]
  ))


  val all: TypeHints = hints


}
