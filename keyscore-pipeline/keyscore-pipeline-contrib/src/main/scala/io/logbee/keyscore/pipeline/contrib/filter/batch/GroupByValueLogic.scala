package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import org.json4s.Formats

import scala.collection.mutable

object GroupByValueLogic extends Described {

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

  override def describe = Descriptor(
    ref = "efbb3b8e-35f4-45ac-87be-f454cf3a951c",
    describes = FilterDescriptor(
      name = classOf[GroupByValueLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.BATCH_COMPOSITION),
      parameters = Seq(fieldNameParameter, timeWindowActiveParameter, timeWindowMillisParameter),
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
  private var timeWindowActive = false
  private var timeWindowMillis = 0L

  private var current: Option[(Field, mutable.ListBuffer[Dataset])] = None
  private var next: Option[(Field, Dataset)] = None
  private var pass: Option[Dataset] = None

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    fieldName = configuration.getValueOrDefault(GroupByValueLogic.fieldNameParameter, fieldName)
    timeWindowActive = configuration.getValueOrDefault(GroupByValueLogic.timeWindowActiveParameter, timeWindowActive)
    timeWindowMillis = configuration.getValueOrDefault(GroupByValueLogic.timeWindowMillisParameter, timeWindowMillis)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    val field = dataset.records.flatMap(record => record.fields).find(field => field.name == fieldName)

    if (isFirstMatch(field)) {
      current = Option((field.get, mutable.ListBuffer(dataset)))
    }
    else if (isAnotherMatch(field)) {
      current.get._2 += dataset
    }
    else if (isNewMatch(field)) {
      next = Option((field.get, dataset))
    }
    else {
      pass = Option(dataset)
    }

    pushOutIfAvailable()
  }

  override def onPull(): Unit = {
    pushOutIfAvailable()
  }

  private def pushOutIfAvailable(): Unit = {

    if (!isAvailable(out)) {
      return
    }

    if (pass.isDefined) {
      push(out, pass.get)
      pass = None
    }
    else if (next.isDefined) {
      val datasets = current.get._2
      val records = datasets.flatMap(d => d.records).toList
      push(out, Dataset(datasets.head.metadata, records))
      swapCurrentAndNext()
    }

    if (!hasBeenPulled(in)) {
      pull(in)
    }
  }

  private def isFirstMatch(field: Option[Field]): Boolean = {
    field.isDefined && current.isEmpty && field.get.name == fieldName
  }

  private def isAnotherMatch(field: Option[Field]): Boolean = {
    field.isDefined && current.isDefined && field.get == current.get._1
  }

  private def isNewMatch(field: Option[Field]): Boolean = {
    field.isDefined && current.isDefined && field.get != current.get._1 && field.get.name == fieldName
  }

  private def swapCurrentAndNext(): Unit = {
    current = Option(next.get._1, mutable.ListBuffer(next.get._2))
    next = None
  }
}
