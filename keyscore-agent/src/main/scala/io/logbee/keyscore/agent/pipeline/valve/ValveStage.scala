package io.logbee.keyscore.agent.pipeline.valve

import java.util.UUID

import akka.stream.stage._
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import com.google.protobuf.Duration
import com.google.protobuf.util.Timestamps
import com.google.protobuf.util.Timestamps.between
import io.logbee.keyscore.agent.pipeline.valve.ValvePosition.{Closed, Drain, Open, ValvePosition}
import io.logbee.keyscore.agent.pipeline.valve.ValveStage.{FirstValveTimestamp, PreviousDatasetThroughputTime, PreviousValveTimestamp, TotalDatasetThroughputTime}
import io.logbee.keyscore.agent.util.{MovingMedian, RingBuffer}
import io.logbee.keyscore.model.data._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps

object ValveStage {
  val FirstValveTimestamp = "io.logbee.keyscore.agent.pipeline.valve.VALVE_FIRST_TIMESTAMP"
  val PreviousValveTimestamp = "io.logbee.keyscore.agent.pipeline.valve.VALVE_PREVIOUS_TIMESTAMP"
  val PreviousDatasetThroughputTime = "io.logbee.keyscore.agent.pipeline.valve.DATASET_PREVIOUS_THROUGHPUT_TIME"
  val TotalDatasetThroughputTime = "io.logbee.keyscore.agent.pipeline.valve.DATASET_TOTAL_THROUGHPUT_TIME"
}

/**
  * The '''ValveStage''' is a Stage for internal operations for a ~Filter~.
  * It provides  a number of various methods which enable pausing draining inserting extracting etc.
  * @param bufferLimit
  * @param dispatcher
  */
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
    private val insertBuffer = RingBuffer[Dataset](bufferLimit)
    private val totalThroughputTime = MovingMedian(bufferLimit)
    private val throughputTime = MovingMedian(bufferLimit)

    private var state = ValveState(id, bufferLimit = ringBuffer.limit)

    private val stateCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      update(ValveState(id, state.position, ringBuffer.size, ringBuffer.limit, durationToNanos(throughputTime), durationToNanos(totalThroughputTime)))
      promise.success(state)
    })

    private val positionCallback = getAsyncCallback[(Promise[ValveState], ValvePosition)]({
      case (promise, `Open`) =>
        if (isDraining) ringBuffer.clear()
        update(ValveState(id, Open, ringBuffer.size, ringBuffer.limit, durationToNanos(throughputTime), durationToNanos(totalThroughputTime)))
        pullIn()
        promise.success(state)
        log.debug(s"Valve <$id> is now open.")

      case (promise, `Closed`) =>
        totalThroughputTime.reset()
        throughputTime.reset()
        update(ValveState(id, Closed, ringBuffer.size, ringBuffer.limit, durationToNanos(throughputTime), durationToNanos(totalThroughputTime)))
        promise.success(state)
        log.debug(s"Valve <$id> is now closed.")

      case (promise, `Drain`) =>
        ringBuffer.clear()
        update(ValveState(id, Drain, ringBuffer.size, ringBuffer.limit, durationToNanos(throughputTime), durationToNanos(totalThroughputTime)))
        pullIn()
        promise.success(state)
        log.debug(s"Valve <$id> does now drain.")
      case _ =>
    })

    private val insertCallback = getAsyncCallback[(Promise[ValveState], List[Dataset])]({
      case (promise, datasets) =>
        datasets.foreach(ringBuffer.push)
        update(ValveState(id, state.position, ringBuffer.size, ringBuffer.limit, durationToNanos(throughputTime), durationToNanos(totalThroughputTime)))
        promise.success(state)
        pushOut()
        log.debug(s"Inserted ${datasets.size} datasets into valve <$id>")
    })

    private val extractCallback = getAsyncCallback[(Promise[List[Dataset]], Int)]({
      case (promise, amount) =>
        val datasets = ringBuffer.last(amount)
        promise.success(datasets)
        log.debug(s"Extracted ${datasets.size} datasets from valve <$id>")
    })

    private val clearCallback = getAsyncCallback[Promise[ValveState]]({ promise =>
      ringBuffer.clear()
      promise.success(update(ValveState(id, state.position, ringBuffer.size, ringBuffer.limit, durationToNanos(throughputTime), durationToNanos(totalThroughputTime))))
      log.debug(s"Cleared buffer of valve <$id>")
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

      override def insert(datasets: List[Dataset]): Future[ValveState] = {
        val promise = Promise[ValveState]()
        val list = datasets
        log.debug(s"Inserting ${list.size} datasets into valve <$id>")
        insertCallback.invoke(promise, list)
        promise.future
      }

      override def clear(): Future[ValveState] = {
        val promise = Promise[ValveState]()
        log.debug(s"Clearing buffer of valve <$id>: ")
        clearCallback.invoke(promise)
        promise.future
      }
    }

    setHandlers(in, out, this)

    override def preStart(): Unit = {
      initPromise.success(valveProxy)
    }

    override def onPush(): Unit = {

      ringBuffer.push(grab(in))
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

      if (isAvailable(out) && !isDraining) {
        val dataset = if (insertBuffer.isNonEmpty) Option(insertBuffer.pull()) else if(ringBuffer.isNonEmpty) Option(ringBuffer.pull()) else None

        dataset.foreach( dataset => {
          val labels = mutable.Map.empty[String, Label]
          compute(totalThroughputTime, FirstValveTimestamp, TotalDatasetThroughputTime, dataset, labels)
          compute(throughputTime, PreviousValveTimestamp, PreviousDatasetThroughputTime, dataset, labels)
          push(out, withNewLabels(dataset, labels.toMap))
        })
      }
    }

    private def pullIn(): Unit = {
      if (!hasBeenPulled(in)) {
        pull(in)
      }
    }

    private def update(newState: ValveState): ValveState = {
      if (!state.equals(newState)) {
        state = newState
      }
      state
    }

    private def compute(median: MovingMedian, timestampLabel: String, throughputLabel: String, dataset: Dataset, labels: mutable.Map[String, Label]): Unit = {
      val newTimestamp = Timestamps.fromMillis(System.currentTimeMillis())
      labels.put(timestampLabel, Label(timestampLabel, TimestampValue(newTimestamp)))
      dataset.metadata.labels.find(_.name == timestampLabel) match {
        case Some(Label(_, timestamp: TimestampValue)) =>
          val duration = between(timestamp, newTimestamp)
          labels.put(throughputLabel, Label(throughputLabel, DurationValue(duration)))
          median + duration
        case _ =>
          median + Duration.newBuilder().build()
      }
    }

    private def withNewLabels(dataset: Dataset, labels: Map[String, Label]) = {

      val newLabels = allLabelsExceptFirstValveTimestampIfItIsAlreadyPresent(labels, dataset)
      val retainedLabels = allOtherLabelsAndFirstValveTimestampIfPresent(dataset, labels)

      dataset.copy(MetaData(retainedLabels ++ newLabels))
    }

    private def allLabelsExceptFirstValveTimestampIfItIsAlreadyPresent(labels: Map[String, Label], dataset: Dataset) = {
      val isFirstValveTimestampPresent = dataset.metadata.labels.exists(label => label.name == FirstValveTimestamp)
      labels.values.filter(label => {
        label.name != FirstValveTimestamp || label.name == FirstValveTimestamp && !isFirstValveTimestampPresent
      })
    }

    private def allOtherLabelsAndFirstValveTimestampIfPresent(dataset: Dataset, labels: Map[String, Label]) = {
      dataset.metadata.labels.filter(label => !labels.contains(label.name) || label.name == FirstValveTimestamp)
    }

    private def isOpen: Boolean = state.position == Open || state.position == Drain
    private def isDraining: Boolean = state.position == Drain

    private def isNotDraining: Boolean = state.position == Open || state.position == Closed

    private def durationToNanos(duration: Duration): Long = duration.getSeconds * 1000000L + duration.getNanos
  }
}
