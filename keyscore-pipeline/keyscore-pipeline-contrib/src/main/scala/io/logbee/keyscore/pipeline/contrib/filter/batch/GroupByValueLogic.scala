package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
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

  val windowParameter = ChoiceParameterDescriptor(
    ref = "combineByValue.window",
    info = ParameterInfo(
      displayName = "window.displayName",
      description = "window.description"
    ),
    min = 1,
    max = 1,
    choices = Seq(noWindowChoice, timeWindowChoice)
  )

  val noWindowChoice = Choice(
    name = "NO_WINDOW",
    displayName = TextRef("window.NO_WINDOW.displayName"),
    description = TextRef("window.NO_WINDOW.description")
  )

  val timeWindowChoice = Choice(
    name = "TIME_WINDOW",
    displayName = TextRef("window.TIME_WINDOW.displayName"),
    description = TextRef("window.TIME_WINDOW.description")
  )

  val timeWindowMillisParameter = NumberParameterDescriptor(
    ref = "combineByValue.timeWindowMillis",
    info = ParameterInfo(
      displayName = "timeWindowMillis.displayName",
      description = "timeWindowMillis.description"
    ),
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "efbb3b8e-35f4-45ac-87be-f454cf3a951c",
    describes = FilterDescriptor(
      name = classOf[GroupByValueLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.BATCH_COMPOSITION),
      parameters = Seq(fieldNameParameter, windowParameter, timeWindowMillisParameter),
      icon = Icon.fromClass(classOf[GroupByValueLogic])
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
  private var windowChoice = GroupByValueLogic.noWindowChoice.name
  private var timeWindowMillis = 0L

  private val grid = new DataGrid[Entry]()
  private var pass: Option[Dataset] = None

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    fieldName = configuration.getValueOrDefault(GroupByValueLogic.fieldNameParameter, fieldName)
    windowChoice = configuration.getValueOrDefault(GroupByValueLogic.windowParameter, windowChoice)

    windowChoice match {
      case GroupByValueLogic.timeWindowChoice.name =>
        timeWindowMillis = configuration.getValueOrDefault(GroupByValueLogic.timeWindowMillisParameter, timeWindowMillis)
        schedulePush()
      case GroupByValueLogic.noWindowChoice.name =>
        timeWindowMillis = 0
      case _ =>
        windowChoice = GroupByValueLogic.noWindowChoice.name
        timeWindowMillis = 0
    }
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    val field = dataset.records.flatMap(record => record.fields).find(field => field.name == fieldName)

    if (field.isDefined) {

      grid.insert(field.get, Entry(field.get, dataset, System.currentTimeMillis() + timeWindowMillis))

      if (noWindow) {
        grid.markAll("EXPIRED", entry => !entry.field.equals(field.get))
      }
    }
    else {
      pass = Option(dataset)
    }

    pushOutIfAvailable()
  }

  override def onPull(): Unit = {
    pushOutIfAvailable()
  }

  override protected def onTimer(timerKey: Any): Unit = {
    timerKey match {
      case "timeWindow" =>
        pushOutIfAvailable()
        schedulePush()
      case _ =>
    }
  }

  private def schedulePush(): Unit = {
    scheduleOnce("timeWindow", Duration((timeWindowMillis * 0.5).toLong, MILLISECONDS))
  }

  private def pushOutIfAvailable(): Unit = {

    if (!isAvailable(out)) {
      return
    }

    if (pass.isDefined) {
      push(out, pass.get)
      pass = None
    }
    else {

      if (timeWindow) {
        val current = System.currentTimeMillis()
        grid.markAll("EXPIRED", entry => current > entry.expires)
      }

      if (grid.hasMarked("EXPIRED")) {
        val entry = grid.findMarked("EXPIRED").head
        push(out, Dataset(entry.datasets.head.metadata, entry.datasets.flatMap(_.records)))
        grid.remove(entry.field)
      }
    }

    if (!hasBeenPulled(in)) {
      pull(in)
    }
  }

  private def timeWindow: Boolean = windowChoice == GroupByValueLogic.timeWindowChoice.name
  private def noWindow: Boolean = windowChoice == GroupByValueLogic.noWindowChoice.name

  private class DataGrid[V <: Updatedable[V]] {

    private val data = mutable.HashMap.empty[AnyRef, V]
    private val keys = mutable.ListBuffer.empty[AnyRef]
    private val marks = mutable.HashMap.empty[AnyRef, Set[AnyRef]]

    def insert(key: AnyRef, value: V): Unit = {
      val element = data.get(key)
      if (element.isEmpty) {
        data.put(key, value)
        keys += key
      }
      else {
        data.put(key, element.get.update(value))
      }
    }

    def remove(key: AnyRef): Unit = {
      data.remove(key)
      keys -= key
      marks
        .transform { case (_, keySet) => keySet - key }
        .retain { case (_, value) => value.nonEmpty }
    }

    def findMarked(mark: AnyRef): List[V] = {
      marks
        .getOrElse(mark, List.empty)
        .toList
        .sortWith((a, b) => keys.indexOf(a) < keys.indexOf(b))
        .map(key => data(key))
    }

    def hasMarked(mark: AnyRef): Boolean = {
      marks.contains(mark)
    }

    def markAll(mark: AnyRef, p: V => Boolean): Unit = {

      val values = data
        .filter { case (_, value) => p(value) }
        .keys
        .toSet

      if (values.nonEmpty) {
        marks.put(mark, values)
      }
      else {
        marks.remove(mark)
      }
    }
  }

  object Entry {
    def apply(field: Field, dataset: Dataset, expires: Long): Entry = new Entry(field, List(dataset), expires)
    def apply(field: Field, datasets: List[Dataset], expires: Long): Entry = new Entry(field, datasets, expires)
  }
  case class Entry(field: Field, datasets: List[Dataset], expires: Long) extends Updatedable[Entry] {
    val created: Long = System.currentTimeMillis()

    override def update(other: Entry): Entry = {
      copy(datasets = datasets ++ other.datasets)
    }
  }

  trait Updatedable[U] {
    def update(other: U): U
  }
}
