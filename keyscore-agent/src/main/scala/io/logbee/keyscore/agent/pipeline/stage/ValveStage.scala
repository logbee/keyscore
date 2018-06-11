package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.Dataset

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

trait ValveProxy {
  def state(): Future[ValveState]

  def close(): Future[ValveState]

  def open(): Future[ValveState]

  def last(n:Int = 1) : Future[List[Dataset]]

  def insert(dataset: Dataset*): Future[ValveState]

  //  def drain(drain: Boolean) : Future[]
}

case class ValveState(uuid: UUID, closed: Boolean)

class ValveStage extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[ValveProxy]] {
  private val uuid = UUID.randomUUID()
  private val in = Inlet[Dataset]("inlet")
  private val out = Outlet[Dataset]("outlet")

  override def shape: FlowShape[Dataset, Dataset] = FlowShape(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[ValveProxy]) = {
    val logic = new ValveLogic
    (logic, logic.initPromise.future)
  }

  class ValveLogic extends GraphStageLogic(shape) with InHandler with OutHandler {

    val initPromise = Promise[ValveProxy]
    var pushedDatasets = new mutable.ListBuffer[Dataset]()
    var insertedl
    var isClosed = false

    private val closeValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isClosed = true
      promise.success(ValveState(uuid, closed = isClosed))
    })

    private val openValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isClosed = false
      if(isAvailable(in)) {
        push(out,grab(in))
      } else if (isAvailable(out)) {
        pull(in)
      }
      promise.success(ValveState(uuid, closed = isClosed))
    })

    private val stateCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
        promise.success(ValveState(uuid, isClosed))
    })

    private val lastDatasetCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, n) =>
        promise.success(pushedDatasets.takeRight(n).toList)
    })

    private val insertDatasetCallback = getAsyncCallback[(Promise[ValveState], Dataset *)]({
      case (promise, n) =>


    })

    private val valveProxy = new ValveProxy {

      override def close(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        closeValveCallback.invoke(promise)
        promise.future
      }

      override def open(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        openValveCallback.invoke(promise)
        promise.future
      }

      override def last(n:Int): Future[List[Dataset]] = {
        val promise = Promise[List[Dataset]]()
        lastDatasetCallback.invoke(promise, n)
        promise.future
      }

      override def state(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        stateCallback.invoke(promise)
        promise.future
      }

      override def insert(datasets: Dataset*): Future[ValveState] = {
        val promise = Promise[ValveState]()
        insertDatasetCallback.invoke(promise, datasets)
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      initPromise.success(valveProxy)
    }

    override def onPush(): Unit = {

      if (!isClosed) {
        val dataset = grab(in)
        println(dataset)
        push(out, dataset)
        insert(dataset)

      }
    }

    private def insert(dataset: Dataset) = {
      pushedDatasets += dataset
      if (pushedDatasets.size > 10) {
        pushedDatasets.remove(0)
        }
    }

    override def onPull(): Unit = {
      if (!isClosed) {
        pull(in)
      }
    }
  }

}
