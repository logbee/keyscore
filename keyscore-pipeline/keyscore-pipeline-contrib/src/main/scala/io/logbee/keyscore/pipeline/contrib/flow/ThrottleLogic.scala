package io.logbee.keyscore.pipeline.contrib.flow

import java.lang.System.{currentTimeMillis, nanoTime}

import akka.stream.FlowShape
import akka.stream.stage.StageLogging
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Icon}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{FilterLogic, LogicParameters}
import io.logbee.keyscore.pipeline.commons.CommonCategories

import scala.concurrent.duration._
import scala.language.postfixOps


object ThrottleLogic extends Described {

  val datasetsParameter = NumberParameterDescriptor(
    ref = "throttle.datasets",
    info = ParameterInfo(
      displayName = TextRef("throttle.datasets.displayName"),
      description = TextRef("throttle.datasets.description")
    ),
    defaultValue = 1,
    range = NumberRange(1, 1, Long.MaxValue)
  )

  val timeUnitParameter = ChoiceParameterDescriptor(
    ref = "throttle.timeunit",
    info = ParameterInfo(
      displayName = TextRef("throttle.timeunit.displayName"),
      description = TextRef("throttle.timeunit.description")
    ),
    min = 1,
    max = 1,
    choices = Seq(
      Choice(
        name = "SECOND",
        displayName = TextRef("throttle.timeunit.second.displayName"),
        description = TextRef("throttle.timeunit.second.description")
      ),
      Choice(
        name = "MINUTE",
        displayName = TextRef("throttle.timeunit.minute.displayName"),
        description = TextRef("throttle.timeunit.minute.description")
      ),
      Choice(
        name = "HOUR",
        displayName = TextRef("throttle.timeunit.hour.displayName"),
        description = TextRef("throttle.timeunit.hour.description")
      ),
      Choice(
        name = "DAY",
        displayName = TextRef("throttle.timeunit.day.displayName"),
        description = TextRef("throttle.timeunit.day.description")
      ),
    )
  )

  override def describe = Descriptor(
    ref = "d3b2de0c-4984-48d4-8412-36c7adc85410",
    describes = FilterDescriptor(
      name = classOf[ThrottleLogic].getName,
      displayName = TextRef("throttle.displayName"),
      description = TextRef("throttle.description"),
      categories = Seq(CommonCategories.FLOW),
      parameters = Seq(datasetsParameter, timeUnitParameter),
      icon = Icon.fromClass(classOf[ThrottleLogic])
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.flow.ThrottleLogic",
      Locale.ENGLISH, Locale.GERMAN
    ) ++ CommonCategories.CATEGORY_LOCALIZATION
  )
}

class ThrottleLogic(parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(parameters, shape) with StageLogging {

  private val throttlePull = "throttle.pull"

  private var datasets = 1L
  private var timeunit = "SECOND"

  private var period = 0d
  private var timestamp = currentTimeMillis()

  override def initialize(configuration: Configuration): Unit = {
    configure(configuration)
  }

  override def configure(configuration: Configuration): Unit = {

    datasets = configuration.getValueOrDefault(ThrottleLogic.datasetsParameter, datasets)
    timeunit = configuration.getValueOrDefault(ThrottleLogic.timeUnitParameter, timeunit)

    period = timeunit match {
      case "DAY" => 1 / (datasets / (1000d * 86400))
      case "HOUR" => 1 / (datasets / (1000d * 3600))
      case "MINUTE" => 1 / (datasets / (1000d * 60))
      case _ => 1 / (datasets / 1000d)
    }
  }

  override def onPush(): Unit = {
    push(out, grab(in))
    timestamp = currentTimeMillis()
  }

  override def onPull(): Unit = {

    val delta = currentTimeMillis() - timestamp
    if (delta < period) {
      scheduleOnce(throttlePull, (period - delta) millis)
    }
    else {
      pull(in)
    }
  }

  override protected def onTimer(timerKey: Any): Unit = if (timerKey == throttlePull) {
    pull(in)
  }
}
