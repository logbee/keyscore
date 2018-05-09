package io.logbee.keyscore.agent.stream.contrib.stages

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, StageLogging}
import akka.stream.{Attributes, Inlet, SinkShape}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.sink.Sink

import scala.concurrent.{Future, Promise}


class SinkStage(provider: (FilterConfiguration, SinkShape[Dataset]) => SinkLogic, configuration: FilterConfiguration) extends GraphStageWithMaterializedValue[SinkShape[Dataset], Future[Sink]] {

  private val in = Inlet[Dataset](s"${configuration.id}:inlet")

  override def shape: SinkShape[Dataset] = SinkShape(in)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Sink]) = {

    val logic = provider(configuration, shape)
    (logic, logic.initPromise.future)
  }
}

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
      log.info(s"Updating configuration: $configuration")
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

  def teardown(): Unit = {}
}