package io.logbee.keyscore.pipeline.contrib.filter

import java.time.ZoneId

import akka.stream.FlowShape
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Field, TextValue}
import io.logbee.keyscore.model.descriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.Locale.ENGLISH
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef, TranslationMapping}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.contrib.filter.textmutator.ToTimestampDirective

import scala.io.Source
import scala.jdk.javaapi.CollectionConverters._
import scala.util.Using

object ToTimestampValueLogic extends Described {

  val sourceFieldNameParameter = FieldNameParameterDescriptor(
    ref = "sourceFieldName",
    info = ParameterInfo(
      displayName = TextRef("sourceFieldName.displayName"),
      description = TextRef("sourceFieldName.description")
    ),
    defaultValue = "text",
    hint = PresentField,
    mandatory = true
  )

  val sourceFieldType = ChoiceParameterDescriptor(
    ref = "sourceFieldType",
    info = ParameterInfo(
      displayName = TextRef("sourceFieldType.displayName"),
      description = TextRef("sourceFieldType.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = "TEXT",
        displayName = TextRef("sourceFieldType.text.displayName"),
        description = TextRef("sourceFieldType.text.description")
      ),
      Choice(
        name = "NUMBER_SECONDS",
        displayName = TextRef("sourceFieldType.number-seconds.displayName"),
        description = TextRef("sourceFieldType.number-seconds.description")
      ),
      Choice(
        name = "NUMBER_MILLIS",
        displayName = TextRef("sourceFieldType.number-millis.displayName"),
        description = TextRef("sourceFieldType.number-millis.description")
      )
    )
  )

  val formatParameter = TextParameterDescriptor(
    ref = "format",
    info = ParameterInfo(
      displayName = TextRef("format.displayName"),
      description = TextRef("format.description")
    ),
    defaultValue = "yyyy.MM.dd HH:mm:ss.SSS",
    mandatory = true
  )

  private val zones: Seq[String] = "UTC" +: {

      val zonesToFilter = Using(getClass.getResourceAsStream("/timezones-filter.txt")) { resource =>
        Source.fromInputStream(resource).mkString.linesIterator.toSet
      }.get

      asScala(ZoneId.getAvailableZoneIds).toSeq
        .filterNot(zone => zonesToFilter.contains(zone))
        .sorted
  }

  val sourceTimeZoneParameter = ChoiceParameterDescriptor(
    ref = "sourceTimeZone",
    info = ParameterInfo(
      displayName = TextRef("sourceTimeZone.displayName"),
      description = TextRef("sourceTimeZone.description"),
    ),
    min = 1,
    max = 1,
    choices = zones.map(zone => Choice(zone, TextRef(zone), TextRef(zone)))
  )

  override def describe: Descriptor = Descriptor(
    ref = "74405f70-b9de-435c-b245-032306ecd7c1",
    describes = FilterDescriptor(
      name = classOf[ToTimestampValueLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.FIELDS, CommonCategories.CONVERSION),
      parameters = Seq(sourceFieldNameParameter, formatParameter, sourceTimeZoneParameter),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.ToTimestampValueLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ Localization(
          locales = Set(ENGLISH),
          mapping = Map(zones.map(zone => TextRef(zone) ->
            TranslationMapping(Map(
              Locale.ENGLISH -> zone,
              Locale.GERMAN -> zone,
            ))):_*)
        )
      ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class ToTimestampValueLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) {
  
  private var sourceFieldName = ToTimestampValueLogic.sourceFieldNameParameter.defaultValue
  private var format = ToTimestampValueLogic.formatParameter.defaultValue
  private var sourceTimeZone = "UTC"
  
  private var toTimestampDirective = ToTimestampDirective(format)
  
  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {
    sourceFieldName = configuration.getValueOrDefault(ToTimestampValueLogic.sourceFieldNameParameter, sourceFieldName)
    
    format = configuration.getValueOrDefault(ToTimestampValueLogic.formatParameter, format)
    sourceTimeZone = configuration.getValueOrDefault(ToTimestampValueLogic.sourceTimeZoneParameter, sourceTimeZone)
    
    toTimestampDirective = ToTimestampDirective(format, Some(ZoneId.of(sourceTimeZone)))
  }

  override def onPush(): Unit = {
    val dataset = grab(in)
    
    val fieldName = sourceFieldName
    
    push(out, dataset.withRecords(dataset.records.map(record => {
      record.update(_.fields := record.fields.map {
        case field@Field(`fieldName`, TextValue(_)) => toTimestampDirective.invoke(field)
        case field@Field(_, _) => field
      })
    })))
  }

  override def onPull(): Unit = pull(in)
}