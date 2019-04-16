package io.logbee.keyscore.commons.metrics

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.Topics.{FilterMetricsTopic, MetricsTopic}
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

  private val metricsMap: collection.mutable.HashMap[UUID, MetricsCollection] = mutable.HashMap.empty[UUID, MetricsCollection]

  context.system.scheduler.schedule(5 seconds, 2 seconds)(pollMetrics())

  override def receive: Receive = {

    case ScrapeMetricRequest(id) =>
      log.debug(s"Received ScrapeMetricRequest <$id>")
      metricsMap.get(id) match {
        case Some(m: MetricsCollection) => sender ! ScrapedMetricResponse(id, m)
        case None => sender ! ScrapedMetricResponseFailure
      }

    case ScrapedFilterMetrics(filterID, metricsCollection) =>
      log.debug(s"Retrieved metrics for filter <$filterID>")
      metricsMap += (filterID -> metricsCollection)

    case ScrapedFiltersOfPipelineMetrics(pipelineID, metrics) =>
      log.debug(s"Retrieved all metrics of pipeline <$pipelineID>")
      metrics.foreach( i => {
        metricsMap += (i._1 -> i._2)
      })

    case ScrapedFilterMetricsFailure(filterID, e) =>
      log.debug(s"Could not retrieve the metrics of <$filterID>: $e")

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
    mediator ! Publish(FilterMetricsTopic, ScrapeFiltersOfPipelineMetrics)
  }
}
