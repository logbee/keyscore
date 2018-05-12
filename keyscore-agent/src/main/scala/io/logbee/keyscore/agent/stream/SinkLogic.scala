package io.logbee.keyscore.agent.stream

import akka.stream.SinkShape
import akka.stream.stage.{GraphStageLogic, InHandler, StageLogging}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.sink.Sink

import scala.concurrent.{Future, Promise}

abstract class SinkLogic(configuration: FilterConfiguration, shape: SinkShape[Dataset]) extends GraphStageLogic(shape) with InHandler with StageLogging {

  val initPromise = Promise[Sink]

  protected val sink = new Sink {

    private val configureCallback = getAsyncCallback[(FilterConfiguration, Promise[Unit])] {
      case (newConfiguration, promise) =>
        SinkLogic.this.configure(newConfiguration)
        promise.success(())
        log.info(s"Configuration has been updated: $newConfiguration")
    }

    override def configure(configuration: FilterConfiguration): Future[Unit] = {
      val promise = Promise[Unit]()
      log.info(s"Updating sink configuration: $configuration")
      configureCallback.invoke(configuration, promise)
      promise.future
    }
  }

  setHandler(shape.in, this)

  override def preStart(): Unit = {
    log.info(s"Initializing with configuration: $configuration")
    initialize(configuration)
    initPromise.success(sink)
  }

  def initialize(configuration: FilterConfiguration): Unit = {}

  def configure(configuration: FilterConfiguration): Unit
}
