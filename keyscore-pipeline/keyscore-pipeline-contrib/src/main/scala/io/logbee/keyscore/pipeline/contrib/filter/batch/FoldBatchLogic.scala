package io.logbee.keyscore.pipeline.contrib.filter.batch

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Record}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories

import scala.collection.mutable

object FoldBatchLogic extends Described {

  val orderParameter = ChoiceParameterDescriptor(
    ref = "foldBatch.order",
    info = ParameterInfo(
      displayName = TextRef("foldBatch.order.displayName"),
      description = TextRef("foldBatch.order.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = "LEFT",
        displayName = TextRef("foldBatch.order.left.displayName"),
        description = TextRef("foldBatch.order.left.description")
      ),
      Choice(
        name = "RIGHT",
        displayName = TextRef("foldBatch.order.right.displayName"),
        description = TextRef("foldBatch.order.right.description")
      ),
    )
  )

  override def describe = Descriptor(
    ref = "c156f213-6055-42dd-92f1-f1d38f6b8982",
    describes = FilterDescriptor(
      name = classOf[FoldBatchLogic].getName,
      displayName = TextRef("foldBatch.displayName"),
      description = TextRef("foldBatch.description"),
      categories = Seq(CommonCategories.BATCH_COMPOSITION),
      parameters = Seq(orderParameter),
      icon = Icon.fromClass(classOf[FoldBatchLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.batch.FoldBatchLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class FoldBatchLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private var order = "LEFT"

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    order = configuration.getValueOrDefault(FoldBatchLogic.orderParameter, order)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, dataset.update(
      _.records := List(order match {
        case "RIGHT" => fold(dataset.records.reverse)
        case _ => fold(dataset.records)
      })))
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def fold(records: List[Record]): Record = {
    Record(records.foldRight(mutable.ListBuffer.empty[Field]) { case (record, list) =>
      list ++= record.fields.filter(field => !list.exists(other => other.name == field.name))
    }.toList)
  }
}
