package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Icon}
import io.logbee.keyscore.model.descriptor.Maturity.Experimental
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.stage.AbstractGroupingLogic

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

  override def describe = Descriptor(
    ref = "efbb3b8e-35f4-45ac-87be-f454cf3a951c",
    describes = FilterDescriptor(
      name = classOf[GroupByValueLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.BATCH_COMPOSITION),
      parameters = Seq(fieldNameParameter, timeWindowActiveParameter, timeWindowMillisParameter, maxNumberOfGroupsParameter),
      metrics = AbstractGroupingLogic.metrics,
      icon = Icon.fromClass(classOf[GroupByValueLogic]),
      maturity = Experimental
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.batch.GroupByValueLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class GroupByValueLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends AbstractGroupingLogic(parameters, shape) with StageLogging {

  private var fieldName = ""
  private var timeWindowActiveValue = false
  private var timeWindowMillisValue = 0L
  private var maxNumberOfGroupsValue = Long.MaxValue
  private var lastField: Option[Field] = None

  override def configure(configuration: Configuration): Unit = {

    fieldName = configuration.getValueOrDefault(GroupByValueLogic.fieldNameParameter, fieldName)
    timeWindowActiveValue = configuration.getValueOrDefault(GroupByValueLogic.timeWindowActiveParameter, timeWindowActiveValue)
    maxNumberOfGroupsValue = configuration.getValueOrDefault(GroupByValueLogic.maxNumberOfGroupsParameter, maxNumberOfGroupsValue)

    if (timeWindowActiveValue) {
      timeWindowMillisValue = configuration.getValueOrDefault(GroupByValueLogic.timeWindowMillisParameter, timeWindowMillisValue)
    }
    else {
      timeWindowMillisValue = 0
    }
  }

  override protected def examine(dataset: Dataset): Unit = {

    val field = dataset.records.flatMap(record => record.fields).find(field => field.name == fieldName)

    if (timeWindowActive) {
      examineWithActiveTimeWindow(field, dataset)
    }
    else {
      examineWithInActiveTimeWindow(field, dataset)
    }
  }

  private def examineWithActiveTimeWindow(field: Option[Field], dataset: Dataset): Unit = {
    field match {
      case Some(field) => addToGroup(field.hashCode().toString, dataset)
      case _ => passthrough(dataset)
    }
  }

  private def examineWithInActiveTimeWindow(field: Option[Field], dataset: Dataset): Unit = {
    (lastField, field) match {
      case (None, Some(current)) =>
        lastField = current
        addToGroup(current.hashCode().toString, dataset)

      case (Some(last), Some(current)) =>

        val lastId = last.hashCode().toString

        if (last.equals(current)) {
          addToGroup(lastId, dataset)
        }
        else {
          lastField = current
          val currentId = current.hashCode().toString

          closeGroup(lastId)
          openGroup(currentId)
          addToGroup(currentId, dataset)
        }

      case _ => passthrough(dataset)
    }
  }

  override protected def timeWindowActive: Boolean = timeWindowActiveValue

  override protected def timeWindowMillis: Long = timeWindowMillisValue

  override protected def maxGroups: Long = maxNumberOfGroupsValue
}
