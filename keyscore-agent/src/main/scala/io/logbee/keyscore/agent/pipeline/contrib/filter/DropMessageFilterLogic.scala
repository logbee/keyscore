package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, TextValue}

import scala.collection.mutable

object DropMessageFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.DropMessageFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.DropMessageFilter"
  private val filterId = "2f117a41-8bf1-4830-9228-7342f3f3fd64"

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
        ListParameterDescriptor("messagesToRetain", translatedText.getString("messagesToRetainName"), translatedText.getString("messagesToRetainDescription"),
          TextParameterDescriptor("messageRegEx", translatedText.getString("messageRegExName"), translatedText.getString("messageRegExDescription")))
      ))
  }
}

class DropMessageFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  private var messagesToRetain = List.empty[String]

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters)
      parameter.name match {
        case "messagesToRetain" =>
          messagesToRetain = parameter.value.asInstanceOf[List[String]]
        case _ =>
      }

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
