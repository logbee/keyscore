package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.stage.{GraphStageLogic, InHandler, StageLogging}
import akka.stream.{Inlet, Materializer, SinkShape}
import io.logbee.keyscore.model.{Dataset, Green}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterState}
import io.logbee.keyscore.model.sink.SinkProxy

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

abstract class SinkLogic(context: StageContext, configuration: FilterConfiguration, shape: SinkShape[Dataset]) extends GraphStageLogic(shape) with InHandler with StageLogging {

  val initPromise = Promise[SinkProxy]

  protected implicit val system: ActorSystem = context.system
  protected implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  protected override implicit lazy val materializer: Materializer = super.materializer

  protected val in: Inlet[Dataset] = shape.in

  private val sink = new SinkProxy {

    private val configureCallback = getAsyncCallback[(FilterConfiguration, Promise[FilterState])] {
      case (newConfiguration, promise) =>
        SinkLogic.this.configure(newConfiguration)
        promise.success(state)
        log.info(s"Configuration has been updated: $newConfiguration")
    }

    override val id: UUID = configuration.id

    override def configure(configuration: FilterConfiguration): Future[FilterState] = {
      val promise = Promise[FilterState]()
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

  def state(): FilterState = FilterState(configuration.id, Green)

}
