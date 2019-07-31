package io.logbee.keyscore.pipeline.contrib.decoder.csv

import akka.stream.FlowShape
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, Icon, Record, TextValue}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories.{CATEGORY_LOCALIZATION, CSV, DECODING}

import scala.Int.MaxValue
import scala.collection.mutable

object CSVDecoderLogic extends Described {

  val sourceFieldNameParameter = FieldNameParameterDescriptor(
    ref = "csv.source-field-name",
    info = ParameterInfo(
      displayName = TextRef("csv.source-field-name.displayName"),
      description = TextRef("csv.source-field-name.description")
    ),
    defaultValue = "message",
    hint = PresentField,
    mandatory = true
  )

  val lineMode = Choice(
    name = "csv.mode.line",
    TextRef("csv.mode.line.displayName"),
    TextRef("csv.mode.line.description")
  )

  val fileMode = Choice(
    name = "csv.mode.file",
    TextRef("csv.mode.file.displayName"),
    TextRef("csv.mode.file.description")
  )

  val modeParameter = ChoiceParameterDescriptor(
    ref = "csv.mode",
    info = ParameterInfo(
      TextRef("csv.mode.displayName"),
      TextRef("csv.mode.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(lineMode, fileMode)
  )

  val delimiterParameter = TextParameterDescriptor(
    ref = "csv.delimiter",
    info = ParameterInfo(
      TextRef("csv.delimiter.displayName"),
      TextRef("csv.delimiter.description")
    ),
    defaultValue = ",",
    mandatory = true
  )

  val headerParameter = TextListParameterDescriptor(
    ref = "csv.header",
    info = ParameterInfo(
      TextRef("csv.header.displayName"),
      TextRef("csv.header.description")
    ),
    min = 1,
    max = MaxValue
  )

  val removeSourceFieldParameter = BooleanParameterDescriptor(
    ref = "csv.remove-source-field",
    info = ParameterInfo(
      TextRef("csv.remove-source-field.displayName"),
      TextRef("csv.remove-source-field.description")
    ),
    defaultValue = true,
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "292d368e-6e50-4c52-aed5-1a6826d78c22",
    describes = FilterDescriptor(
      name = classOf[CSVDecoderLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(DECODING, CSV),
      parameters = Seq(
        sourceFieldNameParameter,
        modeParameter,
        delimiterParameter,
        headerParameter,
        removeSourceFieldParameter
      ),
      icon = Icon.fromClass(classOf[CSVDecoderLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.decoder.csv.CSVDecoder",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class CSVDecoderLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {

  private var sourceFieldName = CSVDecoderLogic.sourceFieldNameParameter.defaultValue
  private var mode: String = CSVDecoderLogic.lineMode.name
  private var headerList: Seq[String] = Seq.empty
  private var delimiter: String = CSVDecoderLogic.delimiterParameter.defaultValue
  private var removeSourceField = CSVDecoderLogic.removeSourceFieldParameter.defaultValue

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    sourceFieldName = configuration.getValueOrDefault(CSVDecoderLogic.sourceFieldNameParameter, sourceFieldName)
    mode = configuration.getValueOrDefault(CSVDecoderLogic.modeParameter, mode)
    headerList = configuration.getValueOrDefault(CSVDecoderLogic.headerParameter, headerList)
    delimiter = configuration.getValueOrDefault(CSVDecoderLogic.delimiterParameter, delimiter)
    removeSourceField = configuration.getValueOrDefault(CSVDecoderLogic.removeSourceFieldParameter, removeSourceField)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    val records = mode match {

      case CSVDecoderLogic.lineMode.name =>

        if (sourceFieldName.nonEmpty && headerList.nonEmpty && delimiter.nonEmpty) {
          lineMode(sourceFieldName, dataset.records)
        }
        else {
          dataset.records
        }

      case CSVDecoderLogic.fileMode.name =>

        if (sourceFieldName.nonEmpty && delimiter.nonEmpty) {
          fileMode(sourceFieldName, dataset.records)
        }
        else {
          dataset.records
        }

      case unknown =>
        log.error(s"Illegal configuration for mode: $unknown")
        dataset.records
    }

    push(out, dataset.withRecords(records))
  }

  private def lineMode(fieldName: String, records: List[Record]): List[Record] = {

    records.map(record => record.update(_.fields := record.fields.foldLeft(mutable.ListBuffer.empty[Field]) {

      case (fields, source @ Field(`fieldName`, TextValue(line))) =>

        fields ++= headerList.zip(line.split(delimiter)).map {
          case (name, value) => Field(name, TextValue(value))
        }

        if (!removeSourceField) {
          fields += source
        }

        fields

      case (fields, field) => fields += field

    }.toList))
  }

  private def fileMode(fieldName: String, records: List[Record]): List[Record] = {

    records.flatMap(record => {
      record.fields.find(field => fieldName.equals(field.name) && field.value.isInstanceOf[TextValue]) match {
        case Some(source @ Field(_, TextValue(content))) =>
          val lines = content.lines.toList
          val header = lines.head.split(delimiter)
          val parsed = lines.tail.map(_.split(delimiter).zip(header).map { case (value, name) => Field(name, TextValue(value)) }).map(Record(_:_*))

          if (removeSourceField) {
            if (record.fields.size > 1) {
              record.withFields(record.fields.filter(source.ne)) +: parsed
            }
            else { // if there is only the source field, drop the whole record
              parsed
            }
          }
          else {
            record +: parsed
          }

        case _ => List(record)
      }
    })
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
