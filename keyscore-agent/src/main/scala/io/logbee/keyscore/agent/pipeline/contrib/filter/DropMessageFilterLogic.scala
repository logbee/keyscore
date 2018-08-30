package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.Locale

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.ToOption.T2OptionT
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, _}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Localization, TextRef}

import scala.Int.MaxValue


object DropMessageFilterLogic extends Described {

  private val retainMessagesParameter = FieldNameListParameterDescriptor(
    ref = "dropMessages.retain",
    info = ParameterInfo(
      displayName = TextRef("messagesToRetainName"),
      description = TextRef("messagesToRetainDescription")
    ),
    descriptor = FieldNameParameterDescriptor(
      hint = PresentField
    ),
    min = 1,
    max = MaxValue
  )

  override def describe = Descriptor(
    uuid = "2f117a41-8bf1-4830-9228-7342f3f3fd64",
    describes = FilterDescriptor(
      name = classOf[DropMessageFilterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(TextRef("category")),
      parameters = Seq()
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.DropMessageFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )


}

class DropMessageFilterLogic(context: StageContext, configuration: Configuration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  private var messagesToRetain = Seq.empty[String]

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    messagesToRetain = configuration.getValueOrDefault(DropMessageFilterLogic.retainMessagesParameter, messagesToRetain)

  }

  override def onPush(): Unit = {
    val dataset = grab(in)
    if (keep(dataset)) {
      push(out, dataset)
    }
    else {
      pull(in)
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def keep(dataset: Dataset): Boolean = {
    for (record <- dataset.records) {
      for (field <- record.fields) {
        field.value match {
          case textValue: TextValue =>
            for (message <- messagesToRetain) {
              message.r.findFirstMatchIn(textValue.value) match {
                case Some(_) => return false
                case _ =>
              }
            }
          case _ =>
        }
      }
    }
    true
  }
}
