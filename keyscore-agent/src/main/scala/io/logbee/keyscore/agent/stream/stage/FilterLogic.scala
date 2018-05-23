package io.logbee.keyscore.agent.stream.stage

import akka.actor.ActorSystem
import akka.stream.{FlowShape, Inlet, Materializer, Outlet}
import akka.stream.stage.{GraphStageLogic, InHandler, OutHandler, StageLogging}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{Filter, FilterConfiguration}

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

abstract class FilterLogic(context:StageContext, configuration:FilterConfiguration,shape: FlowShape[Dataset,Dataset]) extends GraphStageLogic(shape) with InHandler with OutHandler with StageLogging {

  val initPromise = Promise[Filter]


  protected implicit val system: ActorSystem = context.system
  protected implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  protected override implicit lazy val materializer: Materializer = super.materializer

  protected val in: Inlet[Dataset] = shape.in
  protected val out:Outlet[Dataset] = shape.out

  private val filter = new Filter{
    private val configureCallback = getAsyncCallback[(FilterConfiguration,Promise[Unit])]{
      case(newConfiguration, promise) =>
        FilterLogic.this.configure(newConfiguration)
        promise.success(())
        log.info(s"Configuration has been updated: $newConfiguration")
    }

    override def configure(configuration: FilterConfiguration): Future[Unit] = {
      val promise = Promise[Unit]()
      log.info(s"Updating filter configuration: $configuration")
      configureCallback.invoke(configuration, promise)
      promise.future
    }
  }

  setHandlers(in,out,this)

  override def preStart(): Unit = {
    log.info(s"Initializing with configuration: $configuration")
    initialize(configuration)
    initPromise.success(filter)
  }

  def initialize(configuration: FilterConfiguration): Unit = {}

  def configure(configuration: FilterConfiguration): Unit

}
