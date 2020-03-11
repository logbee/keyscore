package io.logbee.keyscore.pipeline.api

import akka.actor.ActorSystem
import akka.stream.stage.{StageLogging, TimerGraphStageLogic}
import akka.stream.{Materializer, Shape}
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.metrics.MetricsCollection
import io.logbee.keyscore.model.notifications.NotificationsCollection
import io.logbee.keyscore.model.pipeline.{FilterState, LogicProxy, StageSupervisor}
import io.logbee.keyscore.pipeline.api.collectors.metrics.{DefaultMetricsCollector, MetricsCollector}
import io.logbee.keyscore.pipeline.api.collectors.notifications.NotificationsCollector

import scala.concurrent.{ExecutionContextExecutor, Promise}

abstract class AbstractLogic[P <: LogicProxy](val parameters: LogicParameters, shape: Shape) extends TimerGraphStageLogic(shape) with StageLogging {

  private[api] val initPromise = Promise[P]

  protected val proxy: P
  protected val supervisor: StageSupervisor = parameters.supervisor

  protected implicit val system: ActorSystem = parameters.context.system
  protected implicit val dispatcher: ExecutionContextExecutor = parameters.context.dispatcher
  protected override implicit lazy val materializer: Materializer = super.materializer
  protected val metrics: MetricsCollector = new DefaultMetricsCollector()
  protected val notifications: NotificationsCollector = new NotificationsCollector(parameters.uuid.toString)

  override def preStart(): Unit = {
    log.info(s"Initializing <${parameters.uuid}> with configuration: ${parameters.configuration}")
    initialize(parameters.configuration)
    initPromise.success(proxy)
  }

  override def postStop(): Unit = {
    log.info(s"Stopped <${parameters.uuid}>.")
  }

  def initialize(configuration: Configuration): Unit

  def configure(configuration: Configuration): Unit

  def state(): FilterState

  def scrapeMetrics(): MetricsCollection = metrics.scrape()

  def scrapeNotifications(): NotificationsCollection = notifications.scrape()
}
