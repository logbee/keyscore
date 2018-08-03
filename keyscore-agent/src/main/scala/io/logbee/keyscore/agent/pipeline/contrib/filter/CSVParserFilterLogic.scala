package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.{Locale, ResourceBundle, UUID}

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object CSVParserFilterLogic extends Described {

  private val filterName = "io.logbee.keyscore.agent.pipeline.contrib.filter.CSVParserFilterLogic"
  private val bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.CSVParserFilter"
  private val filterId = "292d368e-6e50-4c52-aed5-1a6826d78c22"

  override def describe: MetaFilterDescriptor = {
    val descriptorMap = mutable.Map.empty[Locale,FilterDescriptorFragment]
    descriptorMap ++= Map(
      Locale.ENGLISH -> descriptor(Locale.ENGLISH),
      Locale.GERMAN -> descriptor(Locale.GERMAN)
    )

    MetaFilterDescriptor(UUID.fromString(filterId),filterName,descriptorMap.toMap)
  }

  private def descriptor(language:Locale):FilterDescriptorFragment = {
    val translatedText: ResourceBundle = ResourceBundle.getBundle(bundleName,language)
    FilterDescriptorFragment(
      displayName = translatedText.getString("displayName"),
      description = translatedText.getString("description"),
      previousConnection = FilterConnection(isPermitted = true),
      nextConnection = FilterConnection(isPermitted = true),
      parameters = List(
        ListParameterDescriptor("headers",
          translatedText.getString("headerToParse"),
          translatedText.getString("headerToParseDescription"),
          TextParameterDescriptor("headerName",translatedText.getString("fieldKeyNameHeader"), translatedText.getString("fieldKeyDescriptionHeader"))
        ),
        TextParameterDescriptor("separator",translatedText.getString("fieldKeyNameSeparator"), translatedText.getString("fieldKeyDescriptionSeparator"))
      ))
  }
}

class CSVParserFilterLogic(context:StageContext,configuration:FilterConfiguration,shape:FlowShape[Dataset,Dataset]) extends FilterLogic(context,configuration,shape) {

  private var headerList : List[String] = List.empty
  private var separator : String = ""

  override def initialize(configuration: FilterConfiguration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: FilterConfiguration): Unit = {
    for (parameter <- configuration.parameters)
      parameter match {
        case TextParameter("separator", value) => separator = value
        case TextListParameter("headers", value) => headerList = value
        case _ =>
      }
  }

  override def onPush(): Unit = {
    val dataset = grab(shape.in)
    val records = dataset.records.map(record => {
      Record(record.fields.foldLeft(ListBuffer[Field]()) { (fields, field) =>
        field.value match {
          case TextValue(line) if headerList.nonEmpty && separator.nonEmpty =>
            fields ++= headerList.zip(line.split(separator)).map {
              case (name, value) => Field(name, TextValue(value))
            }
          case _ => fields += field
        }
      }.toList)
    })

    push(out, Dataset(dataset.metadata, records))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
