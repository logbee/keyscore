package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.fromString
import java.util.{Locale, ResourceBundle}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object AddFieldsFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilter"
  private val filterId = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610"

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
      previousConnection = FilterConnection(true),
      nextConnection = FilterConnection(true),
      parameters = List(
        MapParameterDescriptor("fieldsToAdd", translatedText.getString("fieldsToAddName"), translatedText.getString("fieldsToAddDescription"),
          TextParameterDescriptor("fieldName", translatedText.getString("fieldKeyName"), translatedText.getString("fieldKeyDescription")),
          TextParameterDescriptor("fieldValue", translatedText.getString("fieldValueName"), translatedText.getString("fieldValueDescription"))
        )
      ))
  }
}

class AddFieldsFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {
  var dataToAdd = scala.collection.mutable.Map[String, Field[_]]()

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters) {
      parameter.name match {
        case "fieldsToAdd" =>
          val dataMap = parameter.value.asInstanceOf[Map[String, String]]
          dataToAdd ++= dataMap.map(pair => (pair._1, TextField(pair._1, pair._2)))
        case _ =>
      }
    }
  }

  override def onPush(): Unit = {

    val dataset = grab(shape.in)
    var listBufferOfRecords = ListBuffer[Record]()
    for (record <- dataset.records) {
      var payload = new mutable.HashMap[String, Field[_]]()
      payload ++= record.payload
      payload ++= dataToAdd
      listBufferOfRecords += new Record(record.id, payload.toMap)
    }
    val listOfRecords = listBufferOfRecords.toList
    push(out, Dataset(listOfRecords))
  }

  override def onPull(): Unit = {
    pull(in)


  }
}
