package io.logbee.keyscore.agent.pipeline.valve

import java.util.UUID

import akka.stream.stage._
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.agent.util.RingBuffer
import io.logbee.keyscore.model.Dataset

import scala.concurrent.{Future, Promise}


class ValveStage(bufferLimit: Int = 10) extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[ValveProxy]] {
  private val id = UUID.randomUUID()
  private val in = Inlet[Dataset]("inlet")
  private val out = Outlet[Dataset]("outlet")

  override def shape: FlowShape[Dataset, Dataset] = FlowShape(in, out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[ValveProxy]) = {
    val logic = new ValveLogic
    (logic, logic.initPromise.future)
  }

  class ValveLogic extends GraphStageLogic(shape) with InHandler with OutHandler with StageLogging {

    val initPromise = Promise[ValveProxy]

    private val ringBuffer = RingBuffer[Dataset](bufferLimit)
    private var state = ValveState(id, bufferLimit = ringBuffer.limit)

    private val stateCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      update(ValveState(id, state.isPaused, state.isDrained, ringBuffer.size, ringBuffer.limit))
      promise.success(state)
    })

    private val pauseCallback = getAsyncCallback[(Promise[ValveState], Boolean)]({
      case (promise, doPause) =>
        update(ValveState(id, doPause, state.isDrained, ringBuffer.size, ringBuffer.limit))
        if(isNotPaused) {
          pullIn()
        }
        promise.success(state)
    })

    private val drainCallback = getAsyncCallback[(Promise[ValveState], Boolean)] {
      case (promise, drain) =>
        ringBuffer.clear()
        pullIn()
        update(ValveState(id, state.isPaused, drain, ringBuffer.size, ringBuffer.limit))
        promise.success(state)
    }

    private val insertCallback = getAsyncCallback[(Promise[ValveState], List[Dataset])]({
      case (promise, datasets) =>
        datasets.foreach(ringBuffer.push)
        pushOut()
        promise.success(update(ValveState(id, isPaused, isDrained, ringBuffer.size, ringBuffer.limit)))
    })

    private val extractCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, amount) =>
        promise.success(ringBuffer.take(amount))
    })

    private val clearBufferCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      ringBuffer.clear()
      promise.success(update(ValveState(id, isPaused, isDrained, ringBuffer.size, ringBuffer.limit)))
    })

    private val valveProxy = new ValveProxy {

      override def state(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        stateCallback.invoke(promise)
        promise.future
      }

      override def pause(doPause: Boolean): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Pausing ValveStage <$id>")
        pauseCallback.invoke(promise, doPause)
        promise.future
      }

      override def drain(doDrain: Boolean): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Draining ValveStage <$id>")
        drainCallback.invoke(promise, doDrain)
        promise.future
      }

      override def extract(amount: Int): Future[List[Dataset]] = {
        val promise = Promise[List[Dataset]]()
        log.debug(s"Extracting data from ValveStage <$id>")
        extractCallback.invoke(promise, amount)
        promise.future
      }

      override def insert(datasets: Dataset*): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Inserting data into ValveStage <$id>")
        insertCallback.invoke(promise, datasets.toList)
        promise.future
      }

      override def clearBuffer(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Clearing buffer of ValveStage <$id>: ")
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

      if (isNotPaused) {
        if (isNotDrained) {
          pushOut()
        }
        if (ringBuffer.isNotFull || isDrained) {
          pullIn()
        }
      }
    }

    override def onPull(): Unit = {
      if (isNotPaused) {
        pullIn()
      }

      while (ringBuffer.isNonEmpty && isNotDrained) {
        pushOut()
      }
    }

    private def isPaused = state.isPaused
    private def isNotPaused = !state.isPaused
    private def isDrained = state.isDrained
    private def isNotDrained = !state.isDrained

    private def pushOut(): Unit = {
      if (isAvailable(out)) {
        push(out, ringBuffer.pull())
      }
    }

    private def pullIn(): Unit = {
      if (!hasBeenPulled(in)) {
        pull(in)
      }
    }

    private def update(newState: ValveState): ValveState = {
      log.debug(s"ValveStage <$id> changed state from: $state to $newState")
      state = newState
      state
    }
  }
}
