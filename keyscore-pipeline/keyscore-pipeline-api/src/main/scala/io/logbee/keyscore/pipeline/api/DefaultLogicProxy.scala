package io.logbee.keyscore.pipeline.api

import java.util.UUID

import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Label
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.model.notifications.NotificationsCollection
import io.logbee.keyscore.model.pipeline.{FilterState, LogicProxy}

import scala.concurrent.{Future, Promise}

class DefaultLogicProxy(val logic: AbstractLogic[_]) extends LogicProxy {

  private val configureCallback = logic.getAsyncCallback[(Configuration, Promise[FilterState])] {
    case (newConfiguration, promise) =>
      logic.configure(newConfiguration)
      try {
        promise.success(logic.state())
        logic.log.info(s"Configuration has been updated: $newConfiguration")
      } catch {
        case e: Throwable =>
          promise.failure(e)
          logic.log.error(e,"Configuration could not be updated!")
      }
  }

  private val stateCallback = logic.getAsyncCallback[Promise[FilterState]]({ promise =>
    promise.success(logic.state())
  })

  private val scrapeMetricsCallback = logic.getAsyncCallback[Promise[MetricsCollection]]({ promise =>
    promise.success(logic.scrapeMetrics())
  })

  private val scrapeNotificationsCallback = logic.getAsyncCallback[Promise[NotificationsCollection]]({ promise =>
    promise.success(logic.scrapeNotifications())
  })

  override val id: UUID = logic.parameters.uuid

  override def configure(configuration: Configuration): Future[FilterState] = {
    val promise = Promise[FilterState]()
    logic.log.info(s"Updating filter configuration: $configuration")
    configureCallback.invoke(configuration, promise)
    promise.future
  }

  override def state(): Future[FilterState] = {
    val promise = Promise[FilterState]()
    stateCallback.invoke(promise)
    promise.future
  }

  override def scrapeMetrics(labels: Set[Label] = Set.empty): Future[MetricsCollection] = {
    val promise = Promise[MetricsCollection]
    scrapeMetricsCallback.invoke(promise)
    promise.future
  }

  override def scrapeNotifications: Future[NotificationsCollection] = {
    val promise = Promise[NotificationsCollection]
    scrapeNotificationsCallback.invoke(promise)
    promise.future
  }
}
