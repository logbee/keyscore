package io.logbee.keyscore.pipeline.contrib

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Icon}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION
import org.json4s.Formats
import org.json4s.native.Serialization.write

object LoggerLogic extends Described {

  private val levelParameter = ChoiceParameterDescriptor(
    ref = "logger.level",
    info = ParameterInfo(
      displayName = TextRef("logger.level.displayName"),
      description = TextRef("logger.level.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = "DEBUG",
        displayName = TextRef("logger.level.debug.displayName"),
        description = TextRef("logger.level.debug.description")
      ),
      Choice(
        name = "INFO",
        displayName = TextRef("logger.level.info.displayName"),
        description = TextRef("logger.level.info.description")
      ),
      Choice(
        name = "WARNING",
        displayName = TextRef("logger.level.warning.displayName"),
        description = TextRef("logger.level.warning.description")
      ),
      Choice(
        name = "ERROR",
        displayName = TextRef("logger.level.error.displayName"),
        description = TextRef("logger.level.error.description")
      )
    )
  )

  private val formatParameter = ChoiceParameterDescriptor(
    ref = "logger.format",
    info = ParameterInfo(
      displayName = TextRef("logger.format.displayName"),
      description = TextRef("logger.format.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = "PLAIN",
        displayName = TextRef("logger.format.plain.displayName"),
        description = TextRef("logger.format.plain.description")
      ),
      Choice(
        name = "JSON",
        displayName = TextRef("logger.format.json.displayName"),
        description = TextRef("logger.format.json.description")
      )
    )
  )

  override def describe = Descriptor(
    ref = "634bce93-64a3-4469-a105-1be441fdc2e0",
    describes = FilterDescriptor(
      name = classOf[LoggerLogic].getName,
      displayName = TextRef("logger.displayName"),
      description = TextRef("logger.description"),
      categories = Seq(CommonCategories.DEBUG),
      parameters = Seq(levelParameter, formatParameter),
      icon = Icon.fromClass(classOf[LoggerLogic]),
      maturity = Maturity.Official
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.LoggerLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CATEGORY_LOCALIZATION
  )
}

class LoggerLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private implicit val jsonFormats: Formats = KeyscoreFormats.formats

  private var level = "INFO"
  private var format = "PLAIN"

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {
    level = configuration.getValueOrDefault(LoggerLogic.levelParameter, level)
    format = configuration.getValueOrDefault(LoggerLogic.formatParameter, format)
  }

  override def onPush(): Unit = {

    val dataset = grab(in)
    val message = format(dataset)

    level match {
      case "DEBUG" => log.debug(s"$message")
      case "INFO" => log.info(s"$message")
      case "WARNING" => log.warning(s"$message")
      case "ERROR" => log.error(s"$message")
      case _ => log.info(s"$message")
    }

    push(out, dataset)
  }

  override def onPull(): Unit = {
    pull(in)
  }

  private def format(dataset: Dataset): String = {
    format match {
      case "PLAIN" => s"$dataset"
      case "JSON" => write(dataset)
      case _ => s"$dataset"
    }
  }
}
