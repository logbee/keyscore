package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.filter.batch.AbstractGroupingLogic.{PassThrough, _}

import scala.collection.mutable
import scala.concurrent.duration.{Duration, MILLISECONDS}

object AbstractGroupingLogic {
  sealed trait GroupingAction
  case class OpenGroup(id: Option[String]) extends GroupingAction
  case class CloseGroupInclusively(id: Option[String]) extends GroupingAction
  case class CloseGroupExclusively(id: Option[String], nextId: Option[String]) extends GroupingAction
  case class AddToGroup(id: Option[String], openGroup: Boolean = true) extends GroupingAction
  case object PassThrough extends GroupingAction
}

abstract class AbstractGroupingLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private val queue = mutable.PriorityQueue.empty[Entry](entryOrdering)
  private val groups = mutable.HashMap.empty[Option[String], GroupEntry]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {

    val dataset = grab(in)

    examine(dataset) match {

      case OpenGroup(id) => openGroup(id, dataset)

      case CloseGroupInclusively(id) =>
        groups.remove(id) match {
          case Some(group @ GroupEntry(_, _)) =>
            group.datasets += dataset
            group.close()
          case _ => // Nothing to do.
        }

      case CloseGroupExclusively(id, nextId) =>
        groups.remove(id) match {
          case Some(group @ GroupEntry(_, _)) =>
            group.close()
            openGroup(nextId, dataset)

          case _ => openGroup(nextId, dataset)
        }

      case AddToGroup(id, doOpenGroup) =>
        groups.get(id) match {
          case Some(group) => group.datasets += dataset
          case _ if doOpenGroup => openGroup(id, dataset)
          case _ => passthrough(dataset)
        }

      case PassThrough =>
        passthrough(dataset)
    }

    if (isAvailable(out)) {
      if (!tryPush() && timeWindowActive && !isTimerActive("timeWindow")) {
        schedulePush()
      }
    }

    if (queue.size <= maxGroups) {
      pull(in)
    }
  }

  override def onPull(): Unit = {
    if (tryPush()) {
      if (!hasBeenPulled(in)) pull(in)
    }
  }

  override protected def onTimer(timerKey: Any): Unit = {
    timerKey match {
      case "timeWindow" =>
        if (!tryPush()) {
          schedulePush()
        }
      case _ =>
    }
  }

  private def openGroup(id: Option[String], dataset: Dataset): Unit = {
    val group = GroupEntry(id, mutable.ListBuffer(dataset))
    queue enqueue group
    groups.put(id, group)
  }

  private def passthrough(dataset: Dataset): Unit = {
    queue enqueue PassThroughEntry(dataset)
  }

  private def tryPush(): Boolean = {

    if (!isAvailable(out)) return false

    queue.headOption match {

      case Some(PassThroughEntry(dataset)) =>
        queue.dequeue()
        push(out, dataset)
        true

      case Some(group @ GroupEntry(id, datasets)) if group.isClosed || timeWindowActive && group.isExpired =>
        queue.dequeue()
        groups.remove(id)
        push(out, Dataset(datasets.head.metadata, datasets.flatMap(_.records).toList))
        true

      case _ => false
    }
  }

  private def schedulePush(): Unit = {
    queue.headOption match {
      case Some(group @ GroupEntry(_, _)) =>
        val timespan = group.expires - System.currentTimeMillis + 100
        scheduleOnce("timeWindow", Duration(timespan, MILLISECONDS))
      case _ =>
    }
  }

  /**
    * Examines the specified [[Dataset]] and determines the [[GroupingAction]] which should be applied.
    *
    * @param dataset a [[Dataset]]
    *
    * @return the [[GroupingAction]] to apply.
    */
  protected def examine(dataset: Dataset): GroupingAction

  /**
    * Returns whether to use a time window.
    *
    * @return {{{true}}} if time window is active, otherwise {{{false}}}.
    */
  protected def timeWindowActive: Boolean

  /**
    * Returns the size of the time window.
    *
    * @return the size of the time window in milli seconds.
    */
  protected def timeWindowMillis: Long

  /**
    * Returns the maximum number of groups.
    *
    * @return the maximum number of groups.
    */
  protected def maxGroups: Long

  private sealed trait Entry

  private def entryOrdering(a: Entry, b: Entry): Int = {
    (a, b) match {
      case (PassThroughEntry(_), PassThroughEntry(_)) => 0
      case (PassThroughEntry(_), GroupEntry(_, _)) => 1
      case (GroupEntry(_, _), PassThroughEntry(_)) => -1
      case (a @ GroupEntry(_, _), b @ GroupEntry(_, _)) =>
        if (timeWindowActive) {
          if (a.expires < b.expires) 1
          else if (a.expires > b.expires) -1
          else {
            if (a.isClosed && b.isClosed) 0
            else if (a.isClosed) 1
            else if (b.isClosed) -1
            else 0
          }
        }
        else if (a.isClosed && b.isClosed) 0
        else if (a.isClosed) 1
        else if (b.isClosed) -1
        else 0
    }
  }

  private case class PassThroughEntry(dataset: Dataset) extends Entry

  private case class GroupEntry(id: Option[String], datasets: mutable.ListBuffer[Dataset]) extends Entry {

    private var closed: Boolean = false

    val created: Long = System.currentTimeMillis

    def expires: Long = created + timeWindowMillis

    def isExpired: Boolean = System.currentTimeMillis > expires

    def close(): Unit = closed = true

    def isClosed: Boolean = closed
  }
}
