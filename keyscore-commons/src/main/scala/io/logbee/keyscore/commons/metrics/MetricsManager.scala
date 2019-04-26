package io.logbee.keyscore.commons.metrics

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import io.logbee.keyscore.commons.cluster.Topics.{FilterMetricsTopic, MetricsTopic}
import io.logbee.keyscore.commons.ehcache.MetricsCache
import io.logbee.keyscore.commons.ehcache.MetricsCache.Configuration

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

object MetricsManager {

  def apply(): Props = Props(new MetricsManager())
}

class MetricsManager extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private val mediator = DistributedPubSub(context.system).mediator

  val cache = MetricsCache(Configuration(context.system.settings.config))

  context.system.scheduler.schedule(5 seconds, 2 seconds)(pollMetrics())

  override def preStart(): Unit = {
    log.debug(s" started with the following cache configuration: ${cache.configuration}")
    mediator ! Subscribe(MetricsTopic, self)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(MetricsTopic, self)
    log.debug(" stopped.")
  }

  override def receive: Receive = {

    case ScrapeMetricRequest(id) =>
      log.debug(s"Received ScrapeMetricRequest <$id>")
      cache.getNewest(id) match {
        case Some(mc) =>
          sender ! ScrapedMetricResponse(id, mc)
        case None =>
          sender ! ScrapedMetricResponseFailure
      }

    case ScrapedFilterMetrics(filterID, metricsCollection) =>
      log.debug(s"Retrieved metrics for filter <$filterID>")
      cache.put(filterID, metricsCollection)

    case ScrapedFiltersOfPipelineMetrics(pipelineID, metrics) =>
      log.debug(s"Retrieved all metrics of pipeline <$pipelineID>")
      metrics.foreach( i => {
        cache.put(i._1, i._2)
      })

    case ScrapedFilterMetricsFailure(filterID, e) =>
      log.debug(s"Could not retrieve the metrics of <$filterID>: $e")

    case ScrapedFiltersOfPipelineMetricsFailure(pipelineID, e) =>
      log.warning(s"Could not retrieve metrics for the pipeline <$pipelineID>: $e")

  }

  private def pollMetrics(): Unit = {
    mediator ! Publish(FilterMetricsTopic, ScrapeFiltersOfPipelineMetrics)
  }
}
