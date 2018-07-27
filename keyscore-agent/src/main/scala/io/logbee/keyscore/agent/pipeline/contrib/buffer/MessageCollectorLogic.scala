package io.logbee.keyscore.agent.pipeline.contrib.buffer

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Field}

import scala.collection.mutable

object MessageCollectorLogic extends Described {
  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.buffer.MessageCollectorLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.MessageCollector"
  private val filterId = "182003a1-b4ee-435f-b486-d7b1f39e1766"


  override def describe: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale, FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )
    MetaFilterDescriptor(fromString(filterId), filterName, descriptorMap.toMap)
  }

  private def descriptor(language: Locale): FilterDescriptorFragment = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName, language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        TextParameterDescriptor("startMessage",translatedText.getString("startMessageName"),translatedText.getString("startMessageDescription")),
        TextParameterDescriptor("endMessage",translatedText.getString("endMessageName"),translatedText.getString("endMessageDescription")),
        IntParameterDescriptor("maxBufferSize",translatedText.getString("maxBufferSizeName"),translatedText.getString("maxBufferSizeDescription"))
      ),
      category = "Buffer"
    )
  }
}

class MessageCollectorLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = ???

  override def onPush(): Unit = {

    val dataset = grab(shape.in)

    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)

  }
}

