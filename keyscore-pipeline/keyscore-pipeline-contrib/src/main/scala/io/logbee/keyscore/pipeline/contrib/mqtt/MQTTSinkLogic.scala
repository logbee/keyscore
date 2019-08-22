package io.logbee.keyscore.pipeline.contrib.mqtt

import akka.stream.SinkShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Icon, TextValue}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import io.logbee.keyscore.pipeline.contrib.mqtt.MQTTSinkLogic.{brokerParameter, fieldNameParameter, topicParameter}
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttException, MqttMessage, MqttTopic}

object MQTTSinkLogic extends Described {

  val brokerParameter = TextParameterDescriptor(
    "mqtt.broker.url",
    ParameterInfo(
      displayName = TextRef("broker.url.displayName"),
      description = TextRef("broker.url.description")),
    validator = StringValidator(
      expression = """^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$""",
    ),
    defaultValue = "tcp://localhost:1883",
    mandatory = true
  )

  val topicParameter = TextParameterDescriptor(
    "mqtt.topic",
    ParameterInfo(
      displayName = TextRef("topic.displayName"),
      description = TextRef("topic.description")),
    defaultValue = "/test/topic",
    mandatory = true
  )

  val fieldNameParameter = FieldNameParameterDescriptor(
    ref = "mqtt.fieldName",
    info = ParameterInfo(
      displayName = TextRef("fieldName.displayName"),
      description = TextRef("fieldName.description")
    ),
    defaultValue = "message",
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "5a0ff317-06f3-4b19-9373-233531d3cdc7",
    describes = SinkDescriptor(
      name = classOf[MQTTSinkLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SINK, Category("MQTT")),
      parameters = Seq(brokerParameter, topicParameter, fieldNameParameter),
      icon = Icon.fromClass(classOf[MQTTSinkLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.mqtt.MQTTSinkLogic",
      Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
  )
}

class MQTTSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {

  private var brokerUrl: String = "tcp://localhost:1883"
  private var topic: String = "/test/topic"
  private var fieldName: String = "message"

  private val clientId: String = MqttClient.generateClientId()
  private var client: MqttClient = _
  private var msgTopic: MqttTopic = _

  override def initialize(configuration: Configuration): Unit = {
    brokerUrl = configuration.getValueOrDefault(brokerParameter, brokerUrl)
    topic = configuration.getValueOrDefault(topicParameter, topic)
    fieldName = configuration.getValueOrDefault(fieldNameParameter, fieldName)

    configure(configuration)

    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {
    try {
      client = new MqttClient(brokerUrl, clientId)

      client.connect()
      msgTopic = client.getTopic(topic)
    }
    catch {
      case me: MqttException => log.error(s"Caught MQTT exception: $me")
      case e => log.error(s"Caught exception: $e")
    }
  }

  override def onPush(): Unit = {
    grab(in).records
      .foreach(record => {
        record.fields.find(field => fieldName == field.name) match {
          case Some(Field(_, textValue @ TextValue(_))) =>
            try {
              val message = new MqttMessage(textValue.value.getBytes())
              msgTopic.publish(message)
            }
            catch {
              case me: MqttException => log.warning(s"Caught MQTT exception: $me")
              case e => log.warning(s"Caught exception: $e")
            }
          case _ =>
            log.debug(s"Couldn't find field with name $fieldName")
        }
      })
    pull(in)
  }

}
