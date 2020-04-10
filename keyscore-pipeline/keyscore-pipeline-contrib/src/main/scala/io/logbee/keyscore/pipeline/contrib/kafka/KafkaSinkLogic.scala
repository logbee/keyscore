package io.logbee.keyscore.pipeline.contrib.kafka

import akka.stream.SinkShape
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION

object KafkaSinkLogic extends Described {
  import io.logbee.keyscore.model.util.ToOption.T2OptionT

  val topicParameter = TextParameterDescriptor(
    ref = "kafka.sink.topic",
    info = ParameterInfo(
      displayName = TextRef("topic.displayName"),
      description = TextRef("topic.description")
    ),
    defaultValue = "topic",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "4fedbe8e-115e-4408-ba53-5b627b6e2eaf",
    describes = SinkDescriptor(
      name = classOf[KafkaSinkLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SINK, Category("Kafka")),
      parameters = Seq(
        KafkaSinkLogicBase.bootstrapServerParameter,
        KafkaSinkLogicBase.bootstrapServerPortParameter,
        topicParameter,
        KafkaSinkLogicBase.dataFieldNameParameter,
        KafkaSinkLogicBase.maxMessageSizeParameter
      ),
      icon = Icon.fromClass(classOf[KafkaSinkLogic]),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = classOf[KafkaSinkLogic].getName,
      Locale.ENGLISH, Locale.GERMAN
    ) ++ KafkaSinkLogicBase.LOCALIZATION ++ CATEGORY_LOCALIZATION
  )
}

class KafkaSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends KafkaSinkLogicBase(parameters, shape) {
  private var topic = KafkaSinkLogic.topicParameter.defaultValue

  override def configure(configuration: Configuration): Unit = {
    topic = configuration.getValueOrDefault(KafkaSinkLogic.topicParameter, topic)
    super.configure(configuration)
  }

  override def onPush(): Unit = {
    val dataset = grab(in)

    addToQueue(dataset)(_ => Some(topic))
  }
}
