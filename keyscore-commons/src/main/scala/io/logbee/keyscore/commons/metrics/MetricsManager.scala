package io.logbee.keyscore.commons.metrics

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.Topics.{MetricsRequestsTopic, MetricsTopic}
import io.logbee.keyscore.commons.pipeline.{PipelineMaterialized, PipelineRemoved}
import io.logbee.keyscore.model.metrics.MetricsCollection

import scala.collection.mutable
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

object MetricsManager {

}

class MetricsManager extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private val mediator = DistributedPubSub(context.system).mediator

  private val pipelineToMetrics: collection.mutable.HashMap[UUID, Map[UUID, MetricsCollection]] = mutable.HashMap.empty[UUID, Map[UUID, MetricsCollection]]
  private val filterToMetrics: collection.mutable.HashMap[UUID, MetricsCollection] = mutable.HashMap.empty[UUID, MetricsCollection]

  context.system.scheduler.schedule(5 seconds, 2 seconds)(pollMetrics())

  override def receive: Receive = {

    case ScrapeFilterMetricRequest(filterID) =>
      log.debug(s"Received ScrapeMetricsRequest for filter <$filterID>")
      filterToMetrics.get(filterID) match {
        case Some(m: MetricsCollection) => sender ! ScrapedFilterMetricResponse(filterID, m)
        case None => sender ! ScrapedFilterMetricResponseFailure
      }

    case ScrapeFiltersOfPipelineMetricsRequest(pipelineID) =>
      log.debug(s"Received ScrapePipelineMetricsRequest for pipeline <$pipelineID>")
      pipelineToMetrics.get(pipelineID) match {
        case Some(map: Map[UUID, MetricsCollection]) => sender ! ScrapedFiltersOfPipelineMetricsResponse(pipelineID, map)
        case None => sender ! ScrapedFiltersOfPipelineMetricsResponseFailure
      }

    case PipelineMaterialized(uuid) =>
      log.debug(s"Created entry for metrics of pipeline <$uuid>")
      pipelineToMetrics + (uuid -> Seq.empty[(UUID, MetricsCollection)])

    case PipelineRemoved(uuid) =>
      log.debug(s"Removed metrics of pipeline <$uuid>")
      pipelineToMetrics - uuid

    case ScrapedFilterMetrics(pipelineID, filterID, metricsCollection) =>
      log.debug(s"Retrieved metrics for filter <$filterID> of pipeline <$pipelineID>")
      filterToMetrics += (filterID -> metricsCollection)
      val updatedMap = pipelineToMetrics(pipelineID) + (filterID -> metricsCollection)
      pipelineToMetrics.updated(pipelineID, updatedMap)

    case ScrapedFiltersOfPipelineMetrics(pipelineID, metrics) =>
      log.debug(s"Retrieved all metrics of pipeline <$pipelineID>")
      pipelineToMetrics += (pipelineID -> metrics)
      metrics.foreach( i => {
        filterToMetrics += (i._1 -> i._2)
      })

    case ScrapedFilterMetricsFailure(pipelineID, filterID, e) =>
      log.debug(s"Could not retrieve the metrics of <$filterID> in the pipeline <$pipelineID>: $e")

    case ScrapedFiltersOfPipelineMetricsFailure(pipelineID, e) =>
      log.warning(s"Could not retrieve metrics for the pipeline <$pipelineID>: $e")

  }

  override def preStart(): Unit = {
    log.debug(" started.")
    mediator ! Subscribe(MetricsTopic, self)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(MetricsTopic, self)
    log.debug(" stopped.")
  }

  private def pollMetrics(): Unit = {
    log.debug("Polling Metrics")
    mediator ! Publish(MetricsRequestsTopic, ScrapeFiltersOfPipelineMetrics)
  }
}
