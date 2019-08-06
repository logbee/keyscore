package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.AnyField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION

import scala.Int.MaxValue
import scala.collection.mutable

object DropRecordsLogic extends Described {

  val fieldNamesParameter = FieldNameListParameterDescriptor(
    ref = "dropRecords.fieldNames",
    info = ParameterInfo(
      displayName = TextRef("fieldNamesDisplayName"),
      description = TextRef("fieldNamesDescription")
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = AnyField
    ),
    min = 1,
    max = MaxValue
  )

  override def describe = Descriptor(
    ref = "2f117a41-8bf1-4830-9228-7342f3f3fd64",
    describes = FilterDescriptor(
      name = classOf[DropRecordsLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.REMOVE_DROP),
      parameters = Seq(fieldNamesParameter),
      icon = Icon.fromClass(classOf[DropRecordsLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.DropRecords",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class DropRecordsLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldNames = Seq.empty[String]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    fieldNames = configuration.getValueOrDefault(DropRecordsLogic.fieldNamesParameter, fieldNames)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, dataset.update(
      _.records := dataset.records.foldLeft(mutable.ListBuffer.empty[Record]) {
        case (result, record) =>
          if (!record.fields.exists(field => fieldNames.contains(field.name))) {
            result += record
          }
          result
      }.toList))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
