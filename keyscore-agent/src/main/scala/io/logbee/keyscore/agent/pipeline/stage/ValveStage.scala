package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.agent.util.Buffer
import io.logbee.keyscore.model.Dataset

import scala.concurrent.{Future, Promise}

trait ValveProxy {
  def state(): Future[ValveState]

  def pause(): Future[ValveState]

  def unpause(): Future[ValveState]

  def extractDatasets(n: Int = 1): Future[List[Dataset]]

  def insert(dataset: Dataset*): Future[ValveState]

  def allowDrain(drainAllowed: Boolean): Future[ValveState]

}

case class ValveState(uuid: UUID, isPaused: Boolean, allowDrain: Boolean = false)


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
    val ringBuffer = Buffer[Dataset](10)
    var isPaused = false
    var allowDrain = false

    private val pauseValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isPaused = true
      promise.success(ValveState(uuid, isPaused = isPaused, allowDrain))
    })

    private val unpauseValveCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      isPaused = false
        if (isAvailable(in)) {
          push(out, grab(in))
        } else if (isAvailable(out)) {
          if (!hasBeenPulled(in)) {
            pull(in)
          }
        }
      promise.success(ValveState(uuid, isPaused = isPaused, allowDrain))
    })

    private val stateCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      promise.success(ValveState(uuid, isPaused, allowDrain))
    })


    private val extractDatasetCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, n) =>
        promise.success(ringBuffer.take(n))
    })

    private val insertDatasetCallback = getAsyncCallback[(Promise[ValveState], List[Dataset])]({
      case (promise, datasets) =>
        datasets.foreach(dataset =>
          ringBuffer.push(dataset))
        promise.success(ValveState(uuid, isPaused, allowDrain))
    })

    private val allowDrainCallback = getAsyncCallback[(Promise[ValveState], Boolean)] {
      case (promise, drainAllowed) =>
        allowDrain = drainAllowed
        promise.success(ValveState(uuid, isPaused, allowDrain))
    }

    private val valveProxy = new ValveProxy {

      override def state(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        stateCallback.invoke(promise)
        promise.future
      }

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

      override def extractDatasets(n: Int): Future[List[Dataset]] = {
        val promise = Promise[List[Dataset]]()
        extractDatasetCallback.invoke(promise, n)
        promise.future
      }

      override def insert(datasets: Dataset*): Future[ValveState] = {
        val promise = Promise[ValveState]()
        insertDatasetCallback.invoke(promise, datasets.toList)
        promise.future
      }

      override def allowDrain(drainAllowed: Boolean): Future[ValveState] = {
        val promise = Promise[ValveState]()
        allowDrainCallback.invoke(promise, drainAllowed)
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      initPromise.success(valveProxy)
    }

    override def onPush(): Unit = {
      if (!isPaused) {
        if (isAvailable(in)) {
          val element = grab(in)
          ringBuffer.push(element)
        }
        if (ringBuffer.isNotFull()) {
          pull(in)
        }
        while (ringBuffer.isNonEmpty() && isAvailable(out)) {
          if (!allowDrain) {
            push(out, ringBuffer.pull())
          } else {
            println("drained element")
            ringBuffer.pull()
          }
        }
      }
    }

    override def onPull(): Unit = {
      if (!hasBeenPulled(in)) {
        pull(in)
      }
    }
  }

}
