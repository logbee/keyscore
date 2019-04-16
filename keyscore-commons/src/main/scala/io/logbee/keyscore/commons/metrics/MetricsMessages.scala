package io.logbee.keyscore.commons.metrics

import java.util.UUID

import io.logbee.keyscore.model.metrics.MetricsCollection

case class ScrapeFilterMetricRequest(filterID: UUID)
case class ScrapedFilterMetricResponse(filterID: UUID, metricsCollection: MetricsCollection)
case class ScrapedFilterMetricResponseFailure(filterID: UUID)

case class ScrapeFiltersOfPipelineMetricsRequest(pipelineID: UUID)
case class ScrapedFiltersOfPipelineMetricsResponse(pipelineID: UUID, metrics: Map[UUID, MetricsCollection])
case class ScrapedFiltersOfPipelineMetricsResponseFailure(pipelineID: UUID)

case class ScrapeFilterMetrics(filterID: UUID)
case class ScrapedFilterMetrics(pipelineID : UUID, filterID: UUID, metricsCollection: MetricsCollection)
case class ScrapedFilterMetricsFailure(pipelineID : UUID, filterID: UUID, e: Throwable)

case object ScrapeFiltersOfPipelineMetrics
case class ScrapedFiltersOfPipelineMetrics(pipelineID : UUID, metrics: Map[UUID, MetricsCollection])
case class ScrapedFiltersOfPipelineMetricsFailure(pipelineID : UUID, e: Throwable)
