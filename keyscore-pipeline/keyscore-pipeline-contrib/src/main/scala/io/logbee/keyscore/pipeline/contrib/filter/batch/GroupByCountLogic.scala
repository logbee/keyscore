package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Icon, Record}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION

import scala.Long.MaxValue
import scala.collection.mutable

object GroupByCountLogic extends Described {

  val amountParameter = NumberParameterDescriptor(
    ref = "amount",
    info = ParameterInfo(
      displayName = TextRef("amount.displayName"),
      description = TextRef("amount.description")
    ),
    defaultValue = 1,
    range = NumberRange(1, 1, MaxValue),
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "3bf6b11f-2fda-40a8-ab93-e3a71d6b132f",
    describes = FilterDescriptor(
      name = classOf[GroupByCountLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.BATCH_COMPOSITION),
      parameters = Seq(amountParameter),
      icon = Icon.fromClass(classOf[GroupByCountLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.batch.GroupByCountLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}
class GroupByCountLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var amount = GroupByCountLogic.amountParameter.defaultValue

  private val buffer = mutable.ListBuffer.empty[Dataset]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    amount = configuration.getValueOrDefault(GroupByCountLogic.amountParameter, amount)
  }

  override def onPush(): Unit = {

    buffer += grab(in)

    if (buffer.size == amount) {

      push(out, Dataset(buffer.last.metadata, buffer.foldLeft(mutable.ListBuffer.empty[Record]) {
        case (result, dataset) => result ++ dataset.records
      }.toList))

      buffer.clear()
    }
    else {
      pull(in)
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
