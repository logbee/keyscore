package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.Locale

import akka.stream.FlowShape
import io.logbee.keyscore.agent.pipeline.contrib.filter.CSVParserFilterLogic.{headerParameter, separatorParameter}
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.ToOption.T2OptionT
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Localization, TextRef}

import scala.Int.MaxValue
import scala.collection.mutable.ListBuffer

object CSVParserFilterLogic extends Described {

  private val headerParameter = TextListParameterDescriptor(
    ref = "csv.header",
    info = ParameterInfo(TextRef("headerToParse"), TextRef("headerToParseDescription")),
    min = 1,
    max = MaxValue
  )

  private val separatorParameter = TextParameterDescriptor(
    ref = "csv.separator",
    info = ParameterInfo(TextRef("fieldKeyNameSeparator"), TextRef("fieldKeyDescriptionSeparator")),
    defaultValue = ",",
    mandatory = true
  )

  override def describe = Descriptor(
    uuid = "292d368e-6e50-4c52-aed5-1a6826d78c22",
    describes = FilterDescriptor(
      name = classOf[CSVParserFilterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(TextRef("category")),
      parameters = Seq(headerParameter, separatorParameter)
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.agent.pipeline.contrib.filter.CSVParserFilter",
      Locale.ENGLISH, Locale.GERMAN
    )
  )
}

class CSVParserFilterLogic(context:StageContext, configuration: Configuration, shape:FlowShape[Dataset,Dataset]) extends FilterLogic(context,configuration,shape) {

  private var headerList : Seq[String] = Seq.empty
  private var separator : String = separatorParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    headerList = configuration.getValueOrDefault(headerParameter, headerList)
    separator = configuration.getValueOrDefault(separatorParameter, separator)
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
