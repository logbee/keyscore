package io.logbee.keyscore.pipeline.contrib.kafka

import akka.stream.SinkShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Icon, TextValue}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION

object DynamicKafkaSinkLogic extends Described {
  import io.logbee.keyscore.model.util.ToOption.T2OptionT

  val topicFieldNameParameter = FieldNameParameterDescriptor(
    ref = "kafka.sink.topic.field",
    info = ParameterInfo(
      displayName = TextRef("topic.field.displayName"),
      description = TextRef("topic.field.description")
    ),
    defaultValue = "topic",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "5f7e1674-168b-4a7d-bb92-a20a7a79e50b",
    describes = SinkDescriptor(
      name = classOf[DynamicKafkaSinkLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SINK, Category("Kafka")),
      parameters = Seq(
        KafkaSinkLogicBase.bootstrapServerParameter,
        KafkaSinkLogicBase.bootstrapServerPortParameter,
        topicFieldNameParameter,
        KafkaSinkLogicBase.dataFieldNameParameter,
        KafkaSinkLogicBase.maxMessageSizeParameter
      ),
      icon = Icon.fromClass(classOf[DynamicKafkaSinkLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = classOf[DynamicKafkaSinkLogic].getName,
      Locale.ENGLISH, Locale.GERMAN
    )
      ++ KafkaSinkLogicBase.LOCALIZATION
      ++ CATEGORY_LOCALIZATION
  )
}

class DynamicKafkaSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends KafkaSinkLogicBase(parameters, shape) {
  private var topicFieldName = DynamicKafkaSinkLogic.topicFieldNameParameter.defaultValue

  override def configure(configuration: Configuration): Unit = {
    topicFieldName = configuration.getValueOrDefault(DynamicKafkaSinkLogic.topicFieldNameParameter, topicFieldName)
    super.configure(configuration)
  }

  override def onPush(): Unit = {
    val dataset = grab(in)

    addToQueue(dataset) { record =>
      record.fields.find(field => topicFieldName == field.name) match {
        case Some(Field(_, topic@TextValue(_, _))) => Some(topic.value)
        case _ => None
      }
    }
  }
}
