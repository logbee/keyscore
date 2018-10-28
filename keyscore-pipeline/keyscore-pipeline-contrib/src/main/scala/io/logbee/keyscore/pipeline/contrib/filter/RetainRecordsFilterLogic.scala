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
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.pipeline.contrib.CommonCategories.CATEGORY_LOCALIZATION

import scala.Int.MaxValue
import scala.collection.mutable

object RetainRecordsFilterLogic extends Described {

  val fieldNamesParameter = FieldNameListParameterDescriptor(
    ref = "retainRecords.fieldNames",
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
    ref = "4c319934-483c-4c2f-ac38-f9d54cc63734",
    describes = FilterDescriptor(
      name = classOf[DropRecordsFilterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.REMOVE_DROP),
      parameters = Seq(fieldNamesParameter),
      icon = Icon.fromClass(classOf[RetainRecordsFilterLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.RetainRecordsFilter",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}
class RetainRecordsFilterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var fieldNames = Seq.empty[String]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    fieldNames = configuration.getValueOrDefault(DropRecordsFilterLogic.fieldNamesParameter, fieldNames)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, dataset.update(
      _.records := dataset.records.foldLeft(mutable.ListBuffer.empty[Record]) {
        case (result, record) =>
          if (fieldNames.forall(name => record.fields.exists(field => name == field.name))) {
            result += record
          }
          result
      }.toList))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
