package io.logbee.keyscore.commons.metrics

import java.util.UUID

import io.logbee.keyscore.model.metrics.MetricsCollection

case class ScrapeMetricRequest(filterID: UUID)
case class ScrapedMetricResponse(filterID: UUID, metricsCollection: MetricsCollection)
case class ScrapedMetricResponseFailure(filterID: UUID)

case class ScrapeFilterMetrics(filterID: UUID)
case class ScrapedFilterMetrics(filterID: UUID, metricsCollection: MetricsCollection)
case class ScrapedFilterMetricsFailure(filterID: UUID, e: Throwable)

case object ScrapeFiltersOfPipelineMetrics
case class ScrapedFiltersOfPipelineMetrics(pipelineID: UUID, metrics: Map[UUID, MetricsCollection])
case class ScrapedFiltersOfPipelineMetricsFailure(pipelineID: UUID, e: Throwable)
