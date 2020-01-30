package io.logbee.keyscore.commons.collectors.metrics

import java.util.UUID

import akka.actor.ActorRef
import io.logbee.keyscore.model.metrics.MetricsCollection

case class RequestMetrics(id: UUID, mq: MetricsQuery)
case class MetricsResponseSuccess(id: UUID, metrics: Seq[MetricsCollection])
case class MetricsResponseFailure(id: UUID)

case class ScrapeMetrics(ref: ActorRef)
