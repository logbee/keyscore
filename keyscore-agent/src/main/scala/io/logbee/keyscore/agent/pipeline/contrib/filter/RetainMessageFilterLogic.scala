package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, TextField}

import scala.collection.mutable

object RetainMessageFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RetainMessageFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RetainMessageFilter"
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

class RetainMessageFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {
  var messagesToRetain = List[String]()

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
    if (checkForRetainCondition(dataset)) {
      push(out, dataset)
    }
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def checkForRetainCondition(dataset: Dataset): Boolean = {
    for (record <- dataset.records) {
      for (field <- record.payload.values) {
        field match {
          case textField: TextField =>
            for (message <- messagesToRetain) {
              message.r.findFirstMatchIn(textField.value) match {
                case Some(_) => return true
                case _ =>
              }
            }
          case _ =>
        }
      }
    }
    false
  }
}
