package io.logbee.keyscore.commons.metrics

import java.time.Duration
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.config.Config
import io.logbee.keyscore.commons.cluster.Topics.MetricsTopic
import io.logbee.keyscore.commons.ehcache.MetricsCache
import io.logbee.keyscore.commons.metrics.MetricsManager.Configuration
import io.logbee.keyscore.model.metrics.MetricsCollection

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.implicitConversions

object MetricsManager {

  def apply(configuration: Configuration): Props = Props(new MetricsManager(configuration))

  object Configuration {
    val root = "keyscore.metrics.manager"

    def apply(config: Config): Configuration = {

      val resolvedConfig = config.getConfig(s"$root")

      new Configuration(
        initialDelay = resolvedConfig.getDuration("initial-delay"),
        interval = resolvedConfig.getDuration("interval")
      )
    }
  }

  case class Configuration(
    initialDelay: Duration,
    interval: Duration
  )
}

class MetricsManager(configuration: Configuration) extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private val mediator = DistributedPubSub(context.system).mediator

  val cache = MetricsCache(MetricsCache.Configuration(context.system.settings.config))

  context.system.scheduler.schedule(initialDelay = configuration.initialDelay, interval = configuration.interval, mediator, Publish(MetricsTopic, ScrapeMetrics(self)))

  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)

  override def preStart(): Unit = {
    log.debug(s" started with the following cache configuration: ${cache.configuration}")
  }

  override def postStop(): Unit = {
    log.debug(" stopped.")
  }

  override def receive: Receive = {

    case RequestMetrics(id, mq) =>
//      log.debug(s"Received ScrapeMetricRequest <$id>")
      cache.getAll(id, earliest = mq.earliestTimestamp, latest = mq.latestTimestamp, mq.limit) match {
        case mcs: Seq[MetricsCollection] =>
          sender ! MetricsResponseSuccess(id, mcs)
        case _ =>
          sender ! MetricsResponseFailure
      }

    case ScrapeMetricsSuccess(metrics) =>
      metrics.foreach( i => {
        cache.put(UUID.fromString(i._1), i._2)
      })

    case ScrapeMetricsFailure(id, e) =>
      log.warning(s"Could not retrieve metrics for  <$id>: $e")
  }
}
