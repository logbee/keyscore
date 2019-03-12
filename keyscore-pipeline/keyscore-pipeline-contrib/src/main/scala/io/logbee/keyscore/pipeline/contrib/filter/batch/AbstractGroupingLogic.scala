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
  case class CloseGroup(id: Option[String]) extends GroupingAction
  case class AddToGroup(id: Option[String], openGroup: Boolean = true) extends GroupingAction
  case object PassThrough extends GroupingAction
}

abstract class AbstractGroupingLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private val passthroughQueue = mutable.Queue.empty[Group]
  private val windowQueue = mutable.Queue.empty[Group]
  private val groups = mutable.HashMap.empty[Option[String], Group]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {

    val dataset = grab(in)

    examine(dataset) match {
      case OpenGroup(id) => openGroup(id, dataset)

      case CloseGroup(id) =>

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
      val pushFailed = !pushNextGroup()
      if (pushFailed && timeWindowActive && !isTimerActive("timeWindow")) {
        schedulePush()
      }
    }

    if (windowQueue.size < maxGroups && passthroughQueue.size < maxGroups) {
      pull(in)
    }
  }

  override def onPull(): Unit = {
    if (pushNextGroup()) {
      if (!hasBeenPulled(in)) pull(in)
    }
  }

  override protected def onTimer(timerKey: Any): Unit = {
    timerKey match {
      case "timeWindow" =>
        if (!pushNextGroup()) {
          val timespan = windowQueue.head.expires - System.currentTimeMillis + 100
          scheduleOnce("timeWindow", Duration(timespan, MILLISECONDS))
        }
      case _ =>
    }
  }

  private def openGroup(id: Option[String], dataset: Dataset) = {
    val group = Group(id, mutable.ListBuffer(dataset))
    windowQueue enqueue group
    groups.put(id, group)
  }

  private def passthrough(dataset: Dataset) = {
    passthroughQueue enqueue Group(None, mutable.ListBuffer(dataset))
  }

  private def pushNextGroup(): Boolean = {
    val passthroughGroupOption = passthroughQueue.headOption
    val windowGroupOption = windowQueue.headOption

    if (passthroughGroupOption.isDefined && windowGroupOption.isDefined) {
      if (passthroughGroupOption.get.created < windowGroupOption.get.created) {
        pushNextPassthroughGroup()
        return true
      }
      else {
        pushNextWindowGroup()
        return true
      }
    }
    else if (passthroughGroupOption.isDefined) {
      pushNextPassthroughGroup()
      return true
    }
    else if (windowGroupOption.isDefined) {
      if (timeWindowActive) {
        if (windowGroupOption.get.isExpired) {
          pushNextWindowGroup()
          return true
        }
      }
      else {
        if (windowQueue.size > 1) {
          pushNextWindowGroup()
          return true
        }
      }
    }

    false
  }

  private def pushNextWindowGroup(): Unit = {
    val group = windowQueue.dequeue()
    groups.remove(group.id)
    pushGroup(group)
  }

  private def pushNextPassthroughGroup(): Unit = {
    val group = passthroughQueue.dequeue()
    pushGroup(group)
  }

  private def pushGroup(group: Group): Unit = {
    push(out, Dataset(group.datasets.head.metadata, group.datasets.flatMap(_.records).toList))
  }

  private def schedulePush(): Unit = {
    val timespan = windowQueue.head.expires - System.currentTimeMillis + 100
    scheduleOnce("timeWindow", Duration(timespan, MILLISECONDS))
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

  private case class Group(id: Option[String], datasets: mutable.ListBuffer[Dataset]) {
    val created: Long = System.currentTimeMillis
    def expires: Long = created + timeWindowMillis
    def isExpired: Boolean = System.currentTimeMillis > expires
  }
}
