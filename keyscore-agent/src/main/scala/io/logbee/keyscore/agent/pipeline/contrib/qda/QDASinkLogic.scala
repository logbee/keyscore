package io.logbee.keyscore.agent.pipeline.contrib.qda


import java.util.{Locale, ResourceBundle, UUID}

import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, SinkShape}
import io.logbee.keyscore.agent.pipeline.stage.{SinkLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
import org.json4s.{Formats, NoTypeHints}

import scala.collection.mutable
import scala.concurrent.Promise
import scala.util.Success

object QDASinkLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.qda.QDASinkLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.QDASinkLogic"
  private val filterId = "1656c453-49f8-44bf-9237-8a8cbdcf5b1b"

  val descriptorMap = mutable.Map.empty[Locale, FilterDescriptorFragment]


  override def describe: MetaFilterDescriptor = {
    val fragments = Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(UUID.fromString(filterId), filterName, fragments)
  }

  private def descriptor(language: Locale) = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = false),
      parameters = List.empty,
      "Sink")
  }

}

class QDASinkLogic(context: StageContext, configuration: FilterConfiguration, shape: SinkShape[Dataset]) extends SinkLogic(context, configuration, shape) {

  implicit val formats: Formats = Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all

  override def postStop(): Unit = {
    log.info("QDA sink is stopping.")
  }

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
    pull(in)
  }

  override def configure(configuration: FilterConfiguration): Unit = ???

  override def onPush(): Unit = {

    grab(shape.in)

  }


}

