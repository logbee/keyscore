package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Described, Record}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RetainFieldsFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RetainFieldsFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.RetainFieldsFilter"
  private val filterId = "99f4aa2a-ee96-4cf9-bda5-261efb3a8ef6"

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
        ListParameterDescriptor("fieldsToRetain", translatedText.getString("fieldsToRetainName"), translatedText.getString("fieldsToRetainDescription"),
          TextParameterDescriptor("fieldName", translatedText.getString("fieldValueName"), translatedText.getString("fieldValueDescription")))
      ))
  }
}

class RetainFieldsFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {
  var fieldsToRetain = List[String]()

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters)
      parameter.name match {
        case "fieldsToRetain" =>
          fieldsToRetain = parameter.value.asInstanceOf[List[String]]
        case _ =>
      }

  }

  override def onPush(): Unit = {
    val dataset = grab(in)

    var listBufferOfRecords = ListBuffer[Record]()
    for (record <- dataset.records) {
      var payload = record.payload.filterKeys(fieldsToRetain.contains(_))
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    push(out, Dataset(listOfRecords))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
