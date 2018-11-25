package io.logbee.keyscore.pipeline.contrib.filter.standalone

import akka.stream.SourceShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.api.{LogicParameters, SourceLogic}
import io.logbee.keyscore.pipeline.contrib.CommonCategories
import io.logbee.keyscore.model.util.ToOption.T2OptionT

object TextInputSourceLogic extends Described {

  val inputDataParameter = TextParameterDescriptor(
    ParameterRef("fieldText"),
    defaultValue = "Some input string",
    mandatory = true
  )

  override def describe =  Descriptor(
    ref = "6a9671d9-93a9-4fe4-b779-b4e1af9a9e6",
    describes = SourceDescriptor(
      name = classOf[TextInputSourceLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.AUGMENT, CommonCategories.SOURCE, CommonCategories.DEBUG),
      parameters = Seq(inputDataParameter)
      //TODO icon
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.TextInputSource",
      Locale.ENGLISH, Locale.GERMAN) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class TextInputSourceLogic(parameters: LogicParameters, shape: SourceShape[Dataset]) extends SourceLogic(parameters, shape) {

  private var inputData = TextInputSourceLogic.inputDataParameter.defaultValue


  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    inputData = configuration.getValueOrDefault(TextInputSourceLogic.inputDataParameter, inputData)
  }

  override def onPull(): Unit = {

    val outData = Dataset(
      records = Record(
        fields = List(Field(
          "input",
          TextValue(inputData)
        ))
      )
    )

    push(out, outData)
  }

  override def postStop(): Unit = {
    log.info("Text Input source is stopping.")
  }
}
