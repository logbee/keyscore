package io.logbee.keyscore.agent.pipeline.valve

import java.util.UUID

import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.agent.util.RingBuffer
import io.logbee.keyscore.model.Dataset

import scala.concurrent.{Future, Promise}


class ValveStage(bufferSize: Int = 10) extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[ValveProxy]] {
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
    val ringBuffer = RingBuffer[Dataset](bufferSize)
    var isPaused = false
    var allowDrain = false

    private val clearBufferCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      ringBuffer.clear()
      promise.success(ValveState(uuid, isPaused = isPaused, allowDrain, ringBuffer))
    })
    private val pauseValveCallback = getAsyncCallback[(Promise[ValveState], Boolean)]({
      case (promise, doPause) =>
      isPaused = doPause
        if(!isPaused) {
          if (!hasBeenPulled(in)) {
            pull(in)
          }
        }
        promise.success(ValveState(uuid, isPaused = isPaused, allowDrain, ringBuffer))
    })

    private val stateCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      promise.success(ValveState(uuid, isPaused, allowDrain, ringBuffer))
    })


    private val extractCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, n) =>
        promise.success(ringBuffer.take(n))
    })

    private val insertCallback = getAsyncCallback[(Promise[ValveState], List[Dataset])]({
      case (promise, datasets) =>
        datasets.foreach(dataset =>
          ringBuffer.push(dataset))
        if (isAvailable(out)) {
          push(out, ringBuffer.pull())
        }
        promise.success(ValveState(uuid, isPaused, allowDrain, ringBuffer))
    })

    private val allowDrainCallback = getAsyncCallback[(Promise[ValveState], Boolean)] {
      case (promise, drainAllowed) =>
        allowDrain = drainAllowed
        ringBuffer.clear()
        if (!hasBeenPulled(in)) {
          pull(in)
        }
        promise.success(ValveState(uuid, isPaused, allowDrain, ringBuffer))
    }

    private val valveProxy = new ValveProxy {

      override def state(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        stateCallback.invoke(promise)
        promise.future
      }

      override def pause(doPause: Boolean): Future[ValveState] = {
        val promise = Promise[ValveState]()
        pauseValveCallback.invoke(promise, doPause)
        promise.future
      }

      override def extract(n: Int): Future[List[Dataset]] = {
        val promise = Promise[List[Dataset]]()
        extractCallback.invoke(promise, n)
        promise.future
      }

      override def insert(datasets: Dataset*): Future[ValveState] = {
        val promise = Promise[ValveState]()
        insertCallback.invoke(promise, datasets.toList)
        promise.future
      }

      override def allowDrain(drainAllowed: Boolean): Future[ValveState] = {
        val promise = Promise[ValveState]()
        allowDrainCallback.invoke(promise, drainAllowed)
        promise.future
      }

      override def clearBuffer(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        clearBufferCallback.invoke(promise)
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      initPromise.success(valveProxy)
    }

    override def onPush(): Unit = {
      val element = grab(in)
      ringBuffer.push(element)

      if (!isPaused) {
        if (isAvailable(out) && !allowDrain) {
          push(out, ringBuffer.pull())
        }
        if (ringBuffer.isNotFull || allowDrain) {
          pull(in)
        }
      }

    }

    override def onPull(): Unit = {
      if (!isPaused) {
        if (!hasBeenPulled(in)) {
          pull(in)
        }
      }

      while (ringBuffer.isNonEmpty && isAvailable(out) && !allowDrain) {
        push(out, ringBuffer.pull())
      }
    }
  }

}
