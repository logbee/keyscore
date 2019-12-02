package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories

import scala.collection.mutable

object RenameFieldFromValueLogic extends Described {

  val sourceFieldNameParameter = FieldNameParameterDescriptor(
    ref = "renameFieldFromValue.sourceFieldName",
    info = ParameterInfo(
      displayName = "sourceFieldName.displayName",
      description = "sourceFieldName.description"
    ),
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  val targetFieldNameParameter = FieldNameParameterDescriptor(
    ref = "renameFieldFromValue.targetFieldName",
    info = ParameterInfo(
      displayName = "targetFieldName.displayName",
      description = "targetFieldName.description"
    ),
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "18adeaba-2d3d-4b02-94c8-7fb426beafa0",
    describes = FilterDescriptor(
      name = classOf[RenameFieldFromValueLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.FIELDS),
      parameters = Seq(sourceFieldNameParameter, targetFieldNameParameter),
      icon = Icon.fromClass(classOf[RenameFieldFromValueLogic]),
      maturity = Maturity.Stable
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.RenameFieldFromValueLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class RenameFieldFromValueLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var sourceFieldName = ""
  private var targetFieldName = ""

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    sourceFieldName = configuration.getValueOrDefault(RenameFieldFromValueLogic.sourceFieldNameParameter, sourceFieldName)
    targetFieldName = configuration.getValueOrDefault(RenameFieldFromValueLogic.targetFieldNameParameter, targetFieldName)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    val sourceFieldName = this.sourceFieldName
    val targetFieldName = this.targetFieldName
    val pair: (Option[TextField], Option[Field]) = (None, None)

    push(out, dataset.update(

      _.records := dataset.records.foldLeft(mutable.ListBuffer.empty[Record]) {

        case (result, record) =>

          val sourceAndTarget = record.fields.foldLeft(pair) {
            case (fields, Field(`sourceFieldName`, value @ TextValue(_, _))) => (Option(TextField(sourceFieldName, value)), fields._2)
            case (fields, field @ Field(`targetFieldName`, _)) => (fields._1, Option(field))
            case (fields, _) => fields
          }

          if (sourceAndTarget._1.isDefined && sourceAndTarget._2.isDefined) {
            val retain = record.fields.filter(field => field.name != sourceAndTarget._2.get.name)
            result += Record(retain :+ Field(sourceAndTarget._1.get.value, sourceAndTarget._2.get.value))
          }
          else {
            result += record
          }

          result

      }.toList
    ))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
