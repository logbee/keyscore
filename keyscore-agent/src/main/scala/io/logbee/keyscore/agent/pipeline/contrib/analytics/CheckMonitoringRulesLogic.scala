package io.logbee.keyscore.agent.pipeline.contrib.analytics

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Field}

import scala.collection.mutable

object CheckMonitoringRulesLogic extends Described {
  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.analytics.CheckMonitoringRulesLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.CheckMonitoringRules"
  private val filterId = "ab27bfae-bdaf-4e14-a40c-fef56bf7e894"

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
      parameters = List.empty,
      category = "Analytics"
    )
  }
}

class CheckMonitoringRulesLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

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
