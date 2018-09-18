package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.Health
import io.logbee.keyscore.model.blueprint.{PipelineBlueprint, SealedBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.configuration.{Configuration, Parameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.pipeline.FilterStatus
import org.json4s.{FullTypeHints, TypeHints}

object KeyscoreTypeHints {

  val hints = FullTypeHints(List(
    classOf[Configuration],
    classOf[Descriptor],
    classOf[FilterDescriptor],
    classOf[SinkDescriptor],
    classOf[SourceDescriptor],
    classOf[PipelineBlueprint],
    classOf[SealedBlueprint],
    classOf[SourceBlueprint],
    classOf[Parameter],
    classOf[ParameterDescriptor],
    classOf[ParameterGroupCondition],
    classOf[FilterStatus],
    classOf[Health],
    classOf[Dataset],
    classOf[Record],
    classOf[MetaData],
    classOf[Label],
    classOf[Value]
  ))


  val all: TypeHints = hints


}
