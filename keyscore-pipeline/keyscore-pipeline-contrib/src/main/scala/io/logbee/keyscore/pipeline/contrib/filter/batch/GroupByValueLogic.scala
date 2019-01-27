package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field}
import io.logbee.keyscore.model.descriptor.Maturity.Experimental
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.metrics.{GaugeMetricDescriptor, MetricsCollection}
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.filter.batch.GroupByValueLogic.numberOfGroupsMetric
import org.json4s.Formats

import scala.collection.mutable
import scala.concurrent.duration.{Duration, MILLISECONDS}

object GroupByValueLogic extends Described {

  import io.logbee.keyscore.model.util.ToOption.T2OptionT

  val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "combineByValue.fieldName",
    info = ParameterInfo(
      displayName = "fieldName.displayName",
      description = "fieldName.description"
    ),
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  val timeWindowActiveParameter = BooleanParameterDescriptor(
    ref = "combineByValue.timeWindowActive",
    info = ParameterInfo(
      displayName = "timeWindowActive.displayName",
      description = "timeWindowActive.description"
    ),
    mandatory = true
  )

  val timeWindowMillisParameter = NumberParameterDescriptor(
    ref = "combineByValue.timeWindowMillis",
    info = ParameterInfo(
      displayName = "timeWindowMillis.displayName",
      description = "timeWindowMillis.description"
    ),
    mandatory = true
  )

  val maxNumberOfGroupsParameter = NumberParameterDescriptor(
    ref = "combineByValue.maxNumberOfGroups",
    info = ParameterInfo(
      displayName = "maxNumberOfGroups.displayName",
      description = "maxNumberOfGroups.description"
    ),
    mandatory = true
  )

  val numberOfGroupsMetric = GaugeMetricDescriptor(
    name = "numberOfGroups",
    displayName = "numberOfGroups.displayName",
    description = "numberOfGroups.description",
  )

  override def describe = Descriptor(
    ref = "efbb3b8e-35f4-45ac-87be-f454cf3a951c",
    describes = FilterDescriptor(
      name = classOf[GroupByValueLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.BATCH_COMPOSITION),
      parameters = Seq(fieldNameParameter, timeWindowActiveParameter, timeWindowMillisParameter, maxNumberOfGroupsParameter),
      metrics = Seq(numberOfGroupsMetric),
      icon = Icon.fromClass(classOf[GroupByValueLogic]),
      maturity = Experimental
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.batch.GroupByValueLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class GroupByValueLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private implicit val jsonFormats: Formats = KeyscoreFormats.formats

  private var fieldName = ""
  private var timeWindowActive = false
  private var timeWindowMillis = 0L
  private var maxNumberOfGroups = Long.MaxValue

  private val passthroughQueue = mutable.Queue.empty[Group]
  private val windowQueue = mutable.Queue.empty[Group]
  private val groups = mutable.HashMap.empty[Field, Group]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldName = configuration.getValueOrDefault(GroupByValueLogic.fieldNameParameter, fieldName)
    timeWindowActive = configuration.getValueOrDefault(GroupByValueLogic.timeWindowActiveParameter, timeWindowActive)
    maxNumberOfGroups = configuration.getValueOrDefault(GroupByValueLogic.maxNumberOfGroupsParameter, maxNumberOfGroups)

    if (timeWindowActive) {
      timeWindowMillis = configuration.getValueOrDefault(GroupByValueLogic.timeWindowMillisParameter, timeWindowMillis)
    }
    else {
      timeWindowMillis = 0
    }
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    val fieldOption = dataset.records.flatMap(record => record.fields).find(field => field.name == fieldName)

    fieldOption match {
      case Some(field) =>
        groups.get(field) match {
          case Some(group) =>
            group.datasets += dataset
          case _ =>
            val group = Group(fieldOption, mutable.ListBuffer(dataset))
            windowQueue enqueue group
            groups.put(field, group)
        }

      case _ => passthroughQueue enqueue Group(None, mutable.ListBuffer(dataset))
    }

    if (isAvailable(out)) {
      val pushFailed = !pushNextGroup()
      if (pushFailed && timeWindowActive && !isTimerActive("timeWindow")) {
        schedulePush()
      }
    }

    if (windowQueue.size < maxNumberOfGroups && passthroughQueue.size < maxNumberOfGroups) {
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

  override def scrape(): MetricsCollection = {
    metrics.collect(numberOfGroupsMetric)
      .min(0)
      .max(maxNumberOfGroups)
      .set(groups.size)

    metrics.get
  }

  private def schedulePush(): Unit = {
    val timespan = windowQueue.head.expires - System.currentTimeMillis + 100
    scheduleOnce("timeWindow", Duration(timespan, MILLISECONDS))
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
    groups.remove(group.field.get)
    pushGroup(group)
  }

  private def pushNextPassthroughGroup(): Unit = {
    val group = passthroughQueue.dequeue()
    pushGroup(group)
  }

  private def pushGroup(group: Group): Unit = {
    push(out, Dataset(group.datasets.head.metadata, group.datasets.flatMap(_.records).toList))
  }

  case class Group(field: Option[Field], datasets: mutable.ListBuffer[Dataset]) {
    val created: Long = System.currentTimeMillis
    def expires: Long = created + timeWindowMillis
    def isExpired: Boolean = System.currentTimeMillis > expires
  }
}
