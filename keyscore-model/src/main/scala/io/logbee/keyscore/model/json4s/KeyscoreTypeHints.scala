package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.configuration.{Configuration, Parameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.metrics._
import io.logbee.keyscore.model.pipeline.FilterStatus
import org.json4s.{FullTypeHints, TypeHints}

object KeyscoreTypeHints {

  val hints = FullTypeHints(List(
    classOf[Configuration],
    classOf[Descriptor],
    classOf[SealedDescriptor],
    classOf[SourceDescriptor],
    classOf[FilterDescriptor],
    classOf[SinkDescriptor],
    classOf[BranchDescriptor],
    classOf[MergeDescriptor],
    classOf[PipelineBlueprint],
    classOf[SealedBlueprint],
    classOf[SourceBlueprint],
    classOf[FilterBlueprint],
    classOf[SinkBlueprint],
    classOf[BranchBlueprint],
    classOf[MergeBlueprint],
    classOf[Parameter],
    classOf[ParameterDescriptor],
    classOf[FieldDirectiveDescriptor],
    classOf[ParameterGroupCondition],
    classOf[MetricDescriptor],
    classOf[Metric],
    classOf[MetricsCollection],
    classOf[GaugeMetric],
    classOf[CounterMetric],
    classOf[Importance],
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
