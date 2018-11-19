package io.logbee.keyscore.pipeline.contrib.decoder

import akka.stream.FlowShape
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.contrib.CommonCategories.{CATEGORY_LOCALIZATION, CSV, DECODING}

import scala.Int.MaxValue
import scala.collection.mutable.ListBuffer

object CSVDecoderLogic extends Described {

  private[decoder] val headerParameter = TextListParameterDescriptor(
    ref = "csv.header",
    info = ParameterInfo(TextRef("headerToParse"), TextRef("headerToParseDescription")),
    min = 1,
    max = MaxValue
  )

  private[decoder] val separatorParameter = TextParameterDescriptor(
    ref = "csv.separator",
    info = ParameterInfo(TextRef("fieldKeyNameSeparator"), TextRef("fieldKeyDescriptionSeparator")),
    defaultValue = ",",
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "292d368e-6e50-4c52-aed5-1a6826d78c22",
    describes = FilterDescriptor(
      name = classOf[CSVDecoderLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(DECODING, CSV),
      parameters = Seq(headerParameter, separatorParameter),
      icon = Icon.fromClass(classOf[CSVDecoderLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.decoder.CSVDecoder",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class CSVDecoderLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var headerList: Seq[String] = Seq.empty
  private var separator: String = CSVDecoderLogic.separatorParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    headerList = configuration.getValueOrDefault(CSVDecoderLogic.headerParameter, headerList)
    separator = configuration.getValueOrDefault(CSVDecoderLogic.separatorParameter, separator)
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
