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
import io.logbee.keyscore.pipeline.contrib.mqtt.DynamicMQTTSinkLogic.{brokerParameter, dataParameter, topicParameter}
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttException, MqttMessage, MqttTopic}

object DynamicMQTTSinkLogic extends Described {

  val brokerParameter = FieldNameParameterDescriptor(
    ref = "mqtt.broker.fieldName",
    info = ParameterInfo(
      displayName = TextRef("broker.displayName"),
      description = TextRef("broker.description")
    ),
    defaultValue = "mqtt.broker",
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  val topicParameter = FieldNameParameterDescriptor(
    ref = "mqtt.topic.fieldName",
    info = ParameterInfo(
      displayName = TextRef("topic.displayName"),
      description = TextRef("topic.description")
    ),
    defaultValue = "mqtt.topic",
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  val dataParameter = FieldNameParameterDescriptor(
    ref = "mqtt.data.fieldName",
    info = ParameterInfo(
      displayName = TextRef("data.displayName"),
      description = TextRef("data.description")
    ),
    defaultValue = "message",
    hint = FieldNameHint.PresentField,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "5ae39f7f-0c8c-4997-b966-52bf3a320011",
    describes = SinkDescriptor(
      name = classOf[DynamicMQTTSinkLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SINK, Category("MQTT")),
      parameters = Seq(brokerParameter, topicParameter, dataParameter),
      icon = Icon.fromClass(classOf[DynamicMQTTSinkLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.mqtt.DynamicMQTTSinkLogic",
      Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
  )
}

class DynamicMQTTSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {

  private var brokerFieldName: String = "mqtt.broker"
  private var topicFieldName: String = "mqtt.topic"
  private var dataFieldName: String = "message"

  private val clientId: String = MqttClient.generateClientId()
  private var client: MqttClient = _
  private var msgTopic: MqttTopic = _

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)

    pull(in)
  }

  override def configure(configuration: Configuration): Unit = {
    brokerFieldName = configuration.getValueOrDefault(brokerParameter, brokerFieldName)
    topicFieldName = configuration.getValueOrDefault(topicParameter, topicFieldName)
    dataFieldName = configuration.getValueOrDefault(dataParameter, dataFieldName)
  }

  override def onPush(): Unit = {
    grab(in).records
      .foreach(record => {
        record.fields.find(field => brokerFieldName == field.name) match {
          case Some(Field(_, broker@TextValue(_))) =>
            record.fields.find(field => topicFieldName == field.name) match {
              case Some(Field(_, topic@TextValue(_))) =>
                record.fields.find(field => dataFieldName == field.name) match {
                  case Some(Field(_, data@TextValue(_))) =>
                    pushDataset(broker, topic, data)
                  case _ =>
                    log.debug(s"Couldn't find field with name $dataFieldName")
                }
              case _ =>
                log.debug(s"Couldn't find field that contains the topic")
            }
          case _ =>
            log.debug(s"Couldn't find field that contains the broker")

        }
      })
    pull(in)
  }


  private def pushDataset(broker: TextValue, topic: TextValue, data: TextValue) = {
    try {
      client = new MqttClient(broker.value, clientId)

      client.connect()
      msgTopic = client.getTopic(topic.value)

      try {
        val message = new MqttMessage(data.value.getBytes())
        msgTopic.publish(message)
        client.disconnect()
      }
      catch {
        case me: MqttException => log.warning(s"Caught MQTT exception: $me | Broker: ${broker.value} | Topic: ${topic.value}")
        case e: Throwable => log.warning(s"Caught exception: $e | Broker: ${broker.value} | Topic: ${topic.value}")
      }
    }
    catch {
      case me: MqttException => log.warning(s"Caught MQTT exception: $me | Broker: ${broker.value} | Topic: ${topic.value}")
      case e: Throwable => log.error(s"Caught exception: $e | Broker: ${broker.value} | Topic: ${topic.value}")
    }
  }
}

