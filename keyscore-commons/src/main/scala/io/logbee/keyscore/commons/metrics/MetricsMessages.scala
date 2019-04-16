package io.logbee.keyscore.commons.metrics

import java.util.UUID

import io.logbee.keyscore.model.metrics.MetricsCollection

case class ScrapeMetricRequest(filterID: UUID)
case class ScrapedMetricResponse(filterID: UUID, metricsCollection: MetricsCollection)
case class ScrapedMetricResponseFailure(filterID: UUID)

case class ScrapePipelineMetricsRequest(pipelineID: UUID)
case class ScrapedPipelineMetricsResponse(pipelineID: UUID, metrics: Map[UUID, MetricsCollection])
case class ScrapedPipelineMetricsResponseFailure(pipelineID: UUID)

case class ScrapeMetrics(filterID: UUID)
case class ScrapedMetrics(pipelineID : UUID, filterID: UUID, metricsCollection: MetricsCollection)
case class ScrapedMetricsFailure(pipelineID : UUID, filterID: UUID, e: Throwable)


case object ScrapePipelineMetrics
case class ScrapedPipelineMetrics(pipelineID : UUID, metrics: Map[UUID, MetricsCollection])
case class ScrapedPipelineMetricsFailure(pipelineID : UUID, e: Throwable)

case class PipelineMaterialized(uuid: UUID)

case class PipelineRemoved(uuid: UUID)