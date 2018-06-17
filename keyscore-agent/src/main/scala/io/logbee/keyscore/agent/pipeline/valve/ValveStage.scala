package io.logbee.keyscore.agent.pipeline.valve

import java.lang.System.nanoTime
import java.util.UUID

import akka.stream.stage._
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import io.logbee.keyscore.agent.pipeline.valve.ValvePosition.{Closed, Drain, Open, ValvePosition}
import io.logbee.keyscore.agent.pipeline.valve.ValveStage.{FirstValveTimestamp, PreviousValveTimestamp}
import io.logbee.keyscore.agent.util.{MovingMedian, RingBuffer}
import io.logbee.keyscore.model.{Dataset, Label}

import scala.concurrent.{ExecutionContext, Future, Promise}

object ValveStage {
  val FirstValveTimestamp = Label[Long]("FIRST_VALVE_TIMESTAMP")
  val PreviousValveTimestamp = Label[Long]("PREVIOUS_VALVE_TIMESTAMP")
}

class ValveStage(bufferLimit: Int = 10)(implicit val dispatcher: ExecutionContext) extends GraphStageWithMaterializedValue[FlowShape[Dataset, Dataset], Future[ValveProxy]] {
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
    private val totalThroughputTime = MovingMedian()
    private val throughputTime = MovingMedian()

    private var state = ValveState(id, bufferLimit = ringBuffer.limit)

    private val stateCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      update(ValveState(id, state.position, ringBuffer.size, ringBuffer.limit, throughputTime, totalThroughputTime))
      promise.success(state)
    })

    private val positionCallback = getAsyncCallback[(Promise[ValveState], ValvePosition)]({
      case (promise, `Open`) =>
        if (isDraining) ringBuffer.clear()
        update(ValveState(id, Open, ringBuffer.size, ringBuffer.limit, throughputTime, totalThroughputTime))
        pullIn()
        promise.success(state)
        log.debug(s"Valve <$id> is now open.")

      case (promise, `Closed`) =>
        totalThroughputTime.reset()
        throughputTime.reset()
        update(ValveState(id, Closed, ringBuffer.size, ringBuffer.limit, throughputTime, totalThroughputTime))
        promise.success(state)
        log.debug(s"Valve <$id> is now closed.")

      case (promise, `Drain`) =>
        ringBuffer.clear()
        update(ValveState(id, Drain, ringBuffer.size, ringBuffer.limit, throughputTime, totalThroughputTime))
        pullIn()
        promise.success(state)
        log.debug(s"Valve <$id> does now drain.")
    })

    private val insertCallback = getAsyncCallback[(Promise[ValveState], List[Dataset])]({
      case (promise, datasets) =>
        datasets.foreach(ringBuffer.push)
        update(ValveState(id, state.position, ringBuffer.size, ringBuffer.limit, throughputTime, totalThroughputTime))
        promise.success(state)
        pushOut()
        log.debug(s"Inserted ${datasets.size} datasets into valve <$id>")
    })

    private val extractCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, amount) =>
        val datasets = ringBuffer.take(amount)
        promise.success(datasets)
        log.debug(s"Extracted ${datasets.size} datasets from valve <$id>")
    })

    private val clearBufferCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      ringBuffer.clear()
      promise.success(update(ValveState(id, state.position, ringBuffer.size, ringBuffer.limit, throughputTime, totalThroughputTime)))
    })

    private val valveProxy = new ValveProxy {

      override def state(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        stateCallback.invoke(promise)
        promise.future
      }

      override def open(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Open valve <$id>")
        positionCallback.invoke(promise, Open)
        promise.future
      }

      override def close(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Close valve <$id>")
        positionCallback.invoke(promise, Closed)
        promise.future
      }

      override def drain(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Drain Valve <$id>")
        positionCallback.invoke(promise, Drain)
        promise.future
      }

      override def extract(amount: Int): Future[List[Dataset]] = {
        val promise = Promise[List[Dataset]]()
        log.debug(s"Extracting $amount datasets from valve <$id>")
        extractCallback.invoke(promise, amount)
        promise.future
      }

      override def insert(datasets: Dataset*): Future[ValveState] = {
        val promise = Promise[ValveState]()
        val list = datasets.toList
        log.debug(s"Inserting ${list.size} datasets into valve <$id>")
        insertCallback.invoke(promise, list)
        promise.future
      }

      override def clearBuffer(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Clearing buffer of valve <$id>: ")
        clearBufferCallback.invoke(promise)
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      initPromise.success(valveProxy)
    }

    override def onPush(): Unit = {

      val dataset = grab(in)

      compute(totalThroughputTime, FirstValveTimestamp, dataset)
      compute(throughputTime, PreviousValveTimestamp, dataset)

      ringBuffer.push(withNewLabels(dataset))

      if (isOpen) {
        pushOut()
      }

      if (ringBuffer.isNotFull || isDraining) {
        pullIn()
      }
    }

    override def onPull(): Unit = {

      if (isOpen || isDraining) {
        pullIn()
      }

      if (isNotDraining) {
        pushOut()
      }
    }

    private def pushOut(): Unit = {
      if (isAvailable(out) && ringBuffer.isNonEmpty) {
        val dataset = ringBuffer.pull()
        push(out, dataset)
      }
    }

    private def pullIn(): Unit = {
      if (!hasBeenPulled(in)) {
        pull(in)
      }
    }

    private def update(newState: ValveState): ValveState = {
      if (!state.equals(newState)) {
        log.info(s"Valve <$id> changed state from: $state to $newState")
        state = newState
      }
      state
    }

    private def compute(median: MovingMedian, timestampLabel: Label[Long], dataset: Dataset): Unit = {
      dataset.label(timestampLabel) match {
        case Some(timestamp) => median + timestamp
        case None => median + 0
      }
    }

    private def withNewLabels(dataset: Dataset) = {
      dataset
        .labelOnce(FirstValveTimestamp, nanoTime())
        .label(PreviousValveTimestamp, nanoTime())
    }

    private def isOpen = state.position == Open || state.position == Drain
    private def isDraining = state.position == Drain
    private def isNotDraining = state.position == Open || state.position == Closed
  }
}
