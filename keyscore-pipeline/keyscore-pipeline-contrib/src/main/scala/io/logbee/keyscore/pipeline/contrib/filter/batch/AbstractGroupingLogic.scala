package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.localization.TextRef
import io.logbee.keyscore.model.metrics.{CounterMetricDescriptor, GaugeMetricDescriptor}
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}

import scala.collection.mutable
import scala.concurrent.duration.{Duration, MILLISECONDS}

object AbstractGroupingLogic {

  val enqueuedPassedThroughEntries = CounterMetricDescriptor(
    name = "enqueued_passed_through_entries",
    displayName = TextRef("enqueuedPassedThroughEntriesName"),
    description = TextRef("enqueuedPassedThroughEntriesDesc")
  )

  val enqueuedGroupEntries = CounterMetricDescriptor(
    name = "enqueued_group_entries",
    displayName = TextRef("enqueuedGroupEntriesName"),
    description = TextRef("enqueuedGroupEntriesDesc")
  )

  val pushedPassedThroughEntries = CounterMetricDescriptor(
    name = "pushed_passed_through_entries",
    displayName = TextRef("pushedPassedThroughEntriesName"),
    description = TextRef("pushedPassedThroughEntriesDesc")
  )

  val pushedGroupEntries = CounterMetricDescriptor(
    name = "pushed_group_entries",
    displayName = TextRef("pushedGroupEntriesName"),
    description = TextRef("pushedGroupEntriesDesc")
  )

  val addedDatasets = CounterMetricDescriptor(
    name = "added_datasets",
    displayName = TextRef("addedDatasetsName"),
    description = TextRef("addedDatasetsDesc")
  )

  val addedGroupEntries = CounterMetricDescriptor(
    name = "added_group_entries",
    displayName = TextRef("addedGroupEntriesName"),
    description = TextRef("addedGroupEntriesDesc")
  )

  val closedGroupEntries = CounterMetricDescriptor(
    name = "closed_group_entries",
    displayName = TextRef("closedGroupEntriesName"),
    description = TextRef("closedGroupEntriesDesc")
  )

  val droppedGroupEntries = CounterMetricDescriptor(
    name = "dropped_group_entries",
    displayName = TextRef("droppedGroupEntriesName"),
    description = TextRef("droppedGroupEntriesDesc")
  )

  val groupEntries = GaugeMetricDescriptor(
    name = "group_entries",
    displayName = TextRef("groupEntriesName"),
    description = TextRef("groupEntriesDesc")
  )

  val queueEntries = GaugeMetricDescriptor(
    name = "queue_entries",
    displayName = TextRef("queueEntriesName"),
    description = TextRef("queueEntriesDesc")
  )
}

abstract class AbstractGroupingLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {
  import AbstractGroupingLogic._

  private val queue = mutable.PriorityQueue.empty[Entry](entryOrdering)
  private val groups = mutable.HashMap.empty[String, GroupEntry]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {

    examine(grab(in))

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

  protected def passthrough(dataset: Dataset): Unit = {
    queue enqueue PassThroughEntry(dataset)
    metrics.collect(enqueuedPassedThroughEntries).increment()
    metrics.collect(queueEntries).set(queue.size)
    metrics.collect(addedDatasets).increment()
  }

  protected def openGroup(id: String): Unit = {
    val group = GroupEntry()
    group.identifier(id)
    queue enqueue group
    metrics.collect(enqueuedGroupEntries).increment()
    metrics.collect(queueEntries).set(queue.size)
    groups.put(id, group)
    metrics.collect(addedGroupEntries).increment()
    metrics.collect(groupEntries).set(groups.size)
  }

  protected def closeGroup(id: String): Unit = {
    groups.remove(id) match {
      case Some(group @ GroupEntry()) =>
        group.identifiers.foreach(groups.remove)
        group.close()
        metrics.collect(closedGroupEntries).increment()
      case _ => // Nothing to do.
    }
  }

  protected def addToGroup(id: String, dataset: Dataset, doOpenGroup: Boolean = true): Unit = {
    groups.get(id) match {
      case Some(group) =>
        group.add(dataset)
        metrics.collect(addedDatasets).increment()
      case _ if doOpenGroup =>
        openGroup(id)
        addToGroup(id, dataset)
      case _ => passthrough(dataset)
    }
  }

  protected def findIdentifiers(id: String): Set[String] = groups.get(id) match {
    case Some(group) => group.identifiers
    case None => Set.empty
  }

  protected def dropGroup(id: String): Unit = {
    groups.remove(id) match {
      case Some(group) =>
        group.identifiers.foreach(groups.remove)
        group.drop()
        metrics.collect(droppedGroupEntries).increment()
      case _ =>
    }
  }

  protected def addIdentifier(existing: String, additional: String): Unit = {
    groups.get(existing) match {
      case Some(group) =>
        groups.put(additional, group)
        group.identifier(additional)
      case None => // Nothing to do.
    }
  }

  /**
    * Examines the specified [[Dataset]].
    *
    * @param dataset a [[Dataset]]
    */
  protected def examine(dataset: Dataset): Unit

  /**
    * Returns a [[Dataset]] which will be pushed out
    *
    * @param datasets a Seq[Dataset]
    * @return
    */
  protected def fold(datasets: Seq[Dataset]): Dataset = {
    Dataset(datasets.head.metadata, datasets.flatMap(_.records).toList)
  }

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

  private def tryPush(): Boolean = {

    if (!isAvailable(out)) return false

    queue.headOption match {

      case Some(PassThroughEntry(dataset)) =>
        queue.dequeue()
        push(out, dataset)
        metrics.collect(pushedPassedThroughEntries).increment()
        true

      case Some(group @ GroupEntry()) if group.isDropped =>
        queue.dequeue()
        false

      case Some(group @ GroupEntry()) if group.isClosed || timeWindowActive && group.isExpired =>
        queue.dequeue()
        group.identifiers.foreach(groups.remove)
        push(out, fold(group.datasets))
        metrics.collect(pushedGroupEntries).increment()
        true

      case _ => false
    }
  }

  private def schedulePush(): Unit = {
    queue.headOption match {
      case Some(group @ GroupEntry()) =>
        val timespan = group.expires - System.currentTimeMillis + 100
        scheduleOnce("timeWindow", Duration(timespan, MILLISECONDS))
      case _ =>
    }
  }

  private sealed trait Entry

  private def entryOrdering(a: Entry, b: Entry): Int = {
    (a, b) match {
      case (PassThroughEntry(_), PassThroughEntry(_)) => 0
      case (PassThroughEntry(_), GroupEntry()) => 1
      case (GroupEntry(), PassThroughEntry(_)) => -1
      case (a @ GroupEntry(), b @ GroupEntry()) =>
        if (a.isDropped) 1
        else if (b.isDropped) -1
        else if (a.isDropped && b.isDropped) 0
        else if (timeWindowActive) {
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

  private case class GroupEntry() extends Entry {

    private val _identifiers = mutable.HashSet.empty[String]
    private val _datasets = mutable.ListBuffer.empty[Dataset]

    private var closed: Boolean = false
    private var dropped: Boolean = false

    val created: Long = System.currentTimeMillis

    def identifier(id: String): Unit = _identifiers += id

    def identifiers: Set[String] = _identifiers.toSet

    def add(dataset: Dataset): Unit = {
      _datasets += dataset
    }

    def add(datasets: Seq[Dataset]): Unit = {
      _datasets ++= datasets
    }

    def datasets: Seq[Dataset] = _datasets

    def expires: Long = created + timeWindowMillis

    def isExpired: Boolean = System.currentTimeMillis > expires

    def close(): Unit = {
      closed = true

    }

    def drop(): Unit = {
      dropped = true
    }

    def isClosed: Boolean = closed

    def isDropped: Boolean = dropped
  }
}
