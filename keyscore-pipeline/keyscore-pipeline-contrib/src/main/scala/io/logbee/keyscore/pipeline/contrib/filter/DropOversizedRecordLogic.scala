package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION

import scala.Long.MaxValue


object DropOversizedRecordLogic extends Described {

  val fieldLimitParameter = NumberParameterDescriptor(
    ref = "field-limit",
    info = ParameterInfo(
      displayName = TextRef("field-limit.displayName"),
      description = TextRef("field-limit.description")
    ),
    range = NumberRange(1, 1, MaxValue),
    defaultValue = 42
  )

  override def describe = Descriptor(
    ref = "a2694ed4-8cec-470f-baa3-471c1c15b317",
    describes = FilterDescriptor(
      name = classOf[DropOversizedRecordLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.REMOVE_DROP),
      parameters = Seq(fieldLimitParameter),
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.DropOversizedRecordLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class DropOversizedRecordLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var fieldLimit = DropOversizedRecordLogic.fieldLimitParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    fieldLimit = configuration.getValueOrDefault(DropOversizedRecordLogic.fieldLimitParameter, fieldLimit)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)
    push(out, dataset.update(_.records := dataset.records.filter(_.fields.size < fieldLimit)))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
