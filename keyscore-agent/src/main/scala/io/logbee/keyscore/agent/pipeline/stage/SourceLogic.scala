package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.stream.SourceShape
import akka.stream.stage.{GraphStageLogic, OutHandler, StageLogging}
import io.logbee.keyscore.model.{Dataset, Green}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.{Future, Promise}

abstract class SourceLogic(context: StageContext, configuration: FilterConfiguration, shape: SourceShape[Dataset]) extends GraphStageLogic(shape) with OutHandler with StageLogging {

  val initPromise = Promise[SourceProxy]

  protected val source = new SourceProxy {

    private val configureCallback = getAsyncCallback[(FilterConfiguration, Promise[FilterState])] {
      case (newConfiguration, promise) =>
        SourceLogic.this.configure(newConfiguration)
        promise.success(state)
        log.info(s"Configuration has been updated: $newConfiguration")
    }

    override val id: UUID = configuration.id

    override def configure(configuration: FilterConfiguration): Future[FilterState] = {
      val promise = Promise[FilterState]()
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

  def state(): FilterState = FilterState(configuration.id, Green)

}
