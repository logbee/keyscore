package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.model.Dataset

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

trait ValveProxy {
  def state(): Future[ValveState]

  def pause(): Future[ValveState]

  def unpause(): Future[ValveState]

  def extractLiveDatasets(n: Int = 1): Future[List[Dataset]]

  def extractInsertedDatasets(n: Int = 1): Future[List[Dataset]]

  def insert(dataset: Dataset*): Future[ValveState]

  def allowPull(): Future[ValveState]

  def denyPull(): Future[ValveState]

  def allowDrain(): Future[ValveState]

  def denyDrain(): Future[ValveState]
}

case class ValveState(uuid: UUID, isPaused: Boolean, allowPull: Boolean = false, allowDrain: Boolean = false)

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
    var insertedDatasets = new mutable.ListBuffer[Dataset]()
    var pulledDatasets = new mutable.ListBuffer[Dataset]()
    var isPaused = false
    var allowPull = false
    var allowDrain = false

    private val pauseValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isPaused = true
      promise.success(ValveState(uuid, isPaused = isPaused, allowPull, allowDrain))
    })

    private val unpauseValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isPaused = false
      if (isAvailable(in)) {
        push(out, grab(in))
      } else if (isAvailable(out)) {
        pull(in)
      }
      promise.success(ValveState(uuid, isPaused = isPaused, allowPull, allowDrain))
    })

    private val stateCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      promise.success(ValveState(uuid, isPaused, allowPull, allowDrain))
    })

    private val extractInsertedDatasetsCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, n) =>
        promise.success(insertedDatasets.takeRight(n).toList)
    })

    private val extractLiveDataDatasetCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, n) =>
        promise.success(pushedDatasets.takeRight(n).toList)
    })

    private val insertDatasetCallback = getAsyncCallback[(Promise[ValveState], List[Dataset])]({
      case (promise, datasets) =>
        println("reached insert callback")
        insertedDatasets ++= datasets
        insertedDatasets.foreach { dataset =>
          println(dataset)
          push(out, dataset)
        }
        insertedDatasets = mutable.ListBuffer.empty
        println("test")
        promise.success(ValveState(uuid, isPaused, allowPull, allowDrain))
    })

    private val allowPullCallback = getAsyncCallback[(Promise[ValveState])]({ promise =>
      allowPull = true
      promise.success(ValveState(uuid, isPaused, allowPull, allowDrain))
    })

    private val denyPullCallback = getAsyncCallback[(Promise[ValveState])]({ promise =>
      allowPull = false
      promise.success(ValveState(uuid, isPaused, allowPull, allowDrain))
    })

    private val allowDrainCallback = getAsyncCallback[(Promise[ValveState])] { promise =>
      allowDrain = true
      promise.success(ValveState(uuid, isPaused, allowPull, allowDrain))
    }

    private val denyDrainCallback = getAsyncCallback[(Promise[ValveState])] { promise =>
      allowDrain = false
      promise.success(ValveState(uuid, isPaused, allowPull, allowDrain))
    }

    private val valveProxy = new ValveProxy {

      override def pause(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        pauseValveCallback.invoke(promise)
        promise.future
      }

      override def unpause(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        unpauseValveCallback.invoke(promise)
        promise.future
      }

      override def extractLiveDatasets(n: Int): Future[List[Dataset]] = {
        val promise = Promise[List[Dataset]]()
        extractLiveDataDatasetCallback.invoke(promise, n)
        promise.future
      }

      override def state(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        stateCallback.invoke(promise)
        promise.future
      }

      override def insert(datasets: Dataset*): Future[ValveState] = {
        val promise = Promise[ValveState]()
        insertDatasetCallback.invoke(promise, datasets.toList)
        promise.future
      }

      override def allowPull(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        allowPullCallback.invoke(promise)
        promise.future
      }

      override def denyPull(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        denyPullCallback.invoke(promise)
        promise.future
      }

      override def extractInsertedDatasets(n: Int): Future[List[Dataset]] = {
        val promise = Promise[List[Dataset]]()
        extractInsertedDatasetsCallback.invoke(promise, n)
        promise.future
      }

      override def allowDrain(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        allowDrainCallback.invoke(promise)
        promise.future
      }

      override def denyDrain(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        denyDrainCallback.invoke(promise)
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      initPromise.success(valveProxy)
    }

    override def onPush(): Unit = {
      if (!isPaused) {
        val dataset = grab(in)
        push(out, dataset)
        storePushedDataset(dataset)
      } else if (allowDrain) {
        val dataset = grab(in)
      }
    }

    private def storePushedDataset(dataset: Dataset) = {
      pushedDatasets += dataset
      if (pushedDatasets.size > 10) {
        pushedDatasets.remove(0)
      }
    }

    override def onPull(): Unit = {
      if (!isPaused) {
        pull(in)
      } else if (allowPull) {
        if (isAvailable(in)) {
          println("pulled in")
          pull(in)
          pulledDatasets += grab(in)
        }
      }
    }
  }

}
