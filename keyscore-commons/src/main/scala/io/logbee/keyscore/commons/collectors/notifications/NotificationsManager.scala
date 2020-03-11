package io.logbee.keyscore.commons.collectors.notifications

import java.time.Duration
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.config.Config
import io.logbee.keyscore.commons.cluster.Topics.NotificationsTopic
import io.logbee.keyscore.commons.collectors.notifications.NotificationsManager.Configuration
import io.logbee.keyscore.commons.ehcache.NotificationsCache
import io.logbee.keyscore.commons.notifications.{ScrapeNotificationsFailure, ScrapeNotificationsSuccess}
import io.logbee.keyscore.model.notifications.NotificationsCollection

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.implicitConversions

object NotificationsManager {

  def apply(configuration: Configuration): Props = Props(new NotificationsManager(configuration))

  object Configuration {
    val root = "keyscore.notifications.manager"

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

class NotificationsManager(configuration: Configuration) extends Actor with ActorLogging {

  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private val mediator = DistributedPubSub(context.system).mediator

  val cache = NotificationsCache(NotificationsCache.Configuration(context.system.settings.config))

  //TODO active or passive ?
  context.system.scheduler.schedule(initialDelay = configuration.initialDelay, interval = configuration.interval, mediator, Publish(NotificationsTopic, ScrapeNotifications(self)))

  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)

  override def preStart(): Unit = {
    log.debug(s" started with the following cache configuration: ${cache.configuration}")
  }

  override def postStop(): Unit = {
    log.debug(" stopped.")
  }

  override def receive: Receive = {

    case RequestNotifications(id, nq) =>
      cache.getAll(id, earliest = nq.earliestTimestamp, latest = nq.latestTimestamp, nq.limit) match {
        case ncs: Seq[NotificationsCollection] if ncs.nonEmpty =>
          sender ! NotificationsResponseSuccess(id, ncs)
        case _ =>
          sender ! NotificationsResponseFailure
      }

    case ScrapeNotificationsSuccess(ns) =>
      ns.foreach(i => {
        cache.put(UUID.fromString(i._1), i._2)
      })

    case ScrapeNotificationsFailure(id, e) =>
      log.warning(s"Could not retrieve notifications for <$id>: $e")

  }
}
