package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.Dataset

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

trait ValveProxy {
  def close(): Future[ValveState]

  def open(): Future[ValveState]

  def last(n:Int = 1) : Future[List[Dataset]]
}

case class ValveState(open: Boolean)

class ValveStage extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[ValveProxy]] {
  private val in = Inlet[Dataset]("inlet")
  private val out = Outlet[Dataset]("outlet")

  override def shape: FlowShape[Dataset, Dataset] = FlowShape(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[ValveProxy]) = {
    val logic = new ValveLogic
    (logic, logic.initPromise.future)
  }

  class ValveLogic extends GraphStageLogic(shape) with InHandler with OutHandler {

    val initPromise = Promise[ValveProxy]
    var datasets = new mutable.ListBuffer[Dataset]()
    var isClosed = false

    private val closeValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isClosed = true
      promise.success(ValveState(open = false))
    })

    private val openValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isClosed = false
      if(isAvailable(in)) {
        push(out,grab(in))
      } else if (isAvailable(out)) {
        pull(in)
      }
      promise.success(ValveState(open = true))
    })

    private val lastDatasetCallback = getAsyncCallback[(Promise[List[Dataset]], Int)] ({
      case (promise, n) =>
        println("lastCallback")
        promise.success(datasets.takeRight(n).toList)
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
        println("lastMethod")
        lastDatasetCallback.invoke(promise, n)
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
        datasets += dataset
        if(datasets.size > 10) {
          datasets.remove(0)
        }

    }

    override def onPull(): Unit = {
      if (!isClosed) {
        pull(in)
      }
    }
  }

}
