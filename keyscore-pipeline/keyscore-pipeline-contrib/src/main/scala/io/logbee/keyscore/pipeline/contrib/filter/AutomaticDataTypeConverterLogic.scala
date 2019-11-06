package io.logbee.keyscore.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.{Descriptor, FilterDescriptor, Maturity}
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

object AutomaticDataTypeConverterLogic extends Described {


  override def describe = Descriptor(
    ref = "9f5a199b-4a8f-4b62-bbdd-23f046f207c7",
    describes = FilterDescriptor(
      name = classOf[AutomaticDataTypeConverterLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.CONVERSION),
      parameters = Seq(),
      maturity = Maturity.Development
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.filter.AutomaticDataTypeConverterLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class AutomaticDataTypeConverterLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private val NUMBER_PATTERN: Regex = "^[+-]?[\\d]+$".r
  private val DECIMAL_PATTERN: Regex = "^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$".r
  private val BOOLEAN_PATTERN: Regex = "^[Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee]$".r

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
  }

  override def onPush(): Unit = {

    val dataset = grab(in)

    push(out, dataset.update(_.records := dataset.records.map { record =>
      record.update(_.fields := record.fields.map {
        case field@Field(name, TextValue(value)) =>
          Try(value match {
            case value@BOOLEAN_PATTERN(_*) => Field(name, BooleanValue(value.toBoolean))
            case value@NUMBER_PATTERN(_*) => Field(name, NumberValue(value.toLong))
            case value@DECIMAL_PATTERN(_*) => Field(name, DecimalValue(value.toDouble))
            case _ => field
          }) match {
            case Success(field) => field
            case Failure(_) => field
          }
        case field => field
      })
    }))
  }

  override def onPull(): Unit = {
    pull(in)
  }
}
