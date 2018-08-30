package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler, StageLogging}
import akka.stream.{FlowShape, Inlet, Materializer, Outlet}
import io.logbee.keyscore.model.Green
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.filter.{FilterProxy, FilterState}

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

abstract class FilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends GraphStageLogic(shape) with InHandler with OutHandler with StageLogging {

  val initPromise = Promise[FilterProxy]

  protected implicit val system: ActorSystem = parameters.context.system
  protected implicit val dispatcher: ExecutionContextExecutor = parameters.context.dispatcher
  protected override implicit lazy val materializer: Materializer = super.materializer

  protected val in: Inlet[Dataset] = shape.in
  protected val out: Outlet[Dataset] = shape.out

  private val filter = new FilterProxy {
    private val configureCallback = getAsyncCallback[(Configuration, Promise[FilterState])] {
      case (newConfiguration, promise) =>
        FilterLogic.this.configure(newConfiguration)
        try {
        promise.success(FilterLogic.this.state())
        log.info(s"Configuration has been updated: $newConfiguration")
        } catch {
          case e: Throwable =>
            promise.failure(e)
            log.error(e,"Configuration could not be updated!")
        }
    }

    private val stateCallback = getAsyncCallback[Promise[FilterState]]({ promise =>
      promise.success(FilterLogic.this.state())
    })

    override val id: UUID = parameters.uuid

    override def configure(configuration: Configuration): Future[FilterState] = {
      val promise = Promise[FilterState]()
      log.info(s"Updating filter configuration: $configuration")
      configureCallback.invoke(configuration, promise)
      promise.future
    }

    override def state(): Future[FilterState] = {
      val promise = Promise[FilterState]()
      stateCallback.invoke(promise)
      promise.future
    }
  }

  setHandlers(in, out, this)

  override def preStart(): Unit = {
    log.info(s"Initializing with configuration: ${parameters.configuration}")
    initialize(parameters.configuration)
    initPromise.success(filter)
  }

  def initialize(configuration: Configuration): Unit = {}

  def configure(configuration: Configuration): Unit

  def state(): FilterState = FilterState(parameters.uuid, Green)
}
