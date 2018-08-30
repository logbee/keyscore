package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.stage.{GraphStageLogic, OutHandler, StageLogging}
import akka.stream.{Materializer, SourceShape}
import io.logbee.keyscore.model.Green
import io.logbee.keyscore.model.filter.FilterState
import io.logbee.keyscore.model.source.SourceProxy

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

abstract class SourceLogic(uuid: UUID, context: StageContext, configuration: FilterConfiguration, shape: SourceShape[Dataset]) extends GraphStageLogic(shape) with OutHandler with StageLogging {

  val initPromise = Promise[SourceProxy]

  protected implicit val system: ActorSystem = context.system
  protected implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  protected override implicit lazy val materializer: Materializer = super.materializer

  protected val source = new SourceProxy {

    private val configureCallback = getAsyncCallback[(FilterConfiguration, Promise[FilterState])] {
      case (newConfiguration, promise) =>
        SourceLogic.this.configure(newConfiguration)
        promise.success(state)
        log.info(s"Configuration has been updated: $newConfiguration")
    }

    override val id: UUID = uuid

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

  def state(): FilterState = FilterState(uuid, Green)

}
