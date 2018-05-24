package io.logbee.keyscore.agent.stream.stage

import akka.stream.SourceShape
import akka.stream.stage.{GraphStageLogic, OutHandler, StageLogging}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.{Future, Promise}

abstract class SourceLogic(context: StageContext, configuration: FilterConfiguration, shape: SourceShape[Dataset]) extends GraphStageLogic(shape) with OutHandler with StageLogging {

  val initPromise = Promise[SourceProxy]

  protected val source = new SourceProxy {

    private val configureCallback = getAsyncCallback[(FilterConfiguration, Promise[Unit])] {
      case (newConfiguration, promise) =>
        SourceLogic.this.configure(newConfiguration)
        promise.success(())
        log.info(s"Configuration has been updated: $newConfiguration")
    }

    override def configure(configuration: FilterConfiguration): Future[Unit] = {
      val promise = Promise[Unit]()
      log.info(s"Updating source configuration: $configuration")
      configureCallback.invoke(configuration, promise)
      promise.future
    }
  }

  setHandler(shape.out, this)

  override def preStart(): Unit = {
    log.info(s"Initializing with configuration: $configuration")
    initialize(configuration)
    initPromise.success(source)
  }

  def initialize(configuration: FilterConfiguration): Unit = {}

  def configure(configuration: FilterConfiguration): Unit
}
