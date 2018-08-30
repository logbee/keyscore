package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.stage.{GraphStageLogic, OutHandler, StageLogging}
import akka.stream.{Materializer, SourceShape}
import io.logbee.keyscore.model.Green
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.filter.FilterState
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

abstract class SourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends GraphStageLogic(shape) with OutHandler with StageLogging {

  val initPromise = Promise[SourceProxy]

  protected implicit val system: ActorSystem = parameters.context.system
  protected implicit val dispatcher: ExecutionContextExecutor = parameters.context.dispatcher
  protected override implicit lazy val materializer: Materializer = super.materializer

  protected val source = new SourceProxy {

    private val configureCallback = getAsyncCallback[(Configuration, Promise[FilterState])] {
      case (newConfiguration, promise) =>
        SourceLogic.this.configure(newConfiguration)
        promise.success(state)
        log.info(s"Configuration has been updated: $newConfiguration")
    }

    override val id: UUID = parameters.uuid

    override def configure(configuration: Configuration): Future[FilterState] = {
      val promise = Promise[FilterState]()
      log.info(s"Updating source configuration: $configuration")
      configureCallback.invoke(configuration, promise)
      promise.future
    }
  }

  setHandler(shape.out, this)

  override def preStart(): Unit = {
    log.info(s"Initializing with configuration: ${parameters.configuration}")
    initialize(parameters.configuration)
    initPromise.success(source)
  }

  def initialize(configuration: Configuration): Unit = {}

  def configure(configuration: Configuration): Unit

  def state(): FilterState = FilterState(parameters.uuid, Green)

}
