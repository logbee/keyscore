package io.logbee.keyscore.pipeline.contrib

import akka.stream.SinkShape
import akka.stream.stage.AsyncCallback
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.{LogicParameters, SinkLogic}
import io.logbee.keyscore.pipeline.commons.CommonCategories
import io.logbee.keyscore.pipeline.commons.CommonCategories.CATEGORY_LOCALIZATION

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

object DiscardSinkLogic extends Described {

  val intervalParameter = NumberParameterDescriptor(
    ref = "drain.interval",
    ParameterInfo(
      displayName = TextRef("drain.interval.displayName"),
      description = TextRef("drain.interval.description")
    ),
    defaultValue = 0,
    range = NumberRange(
      step = 1,
      start = 0,
      end = Long.MaxValue
    ),
    mandatory = true
  )

  override def describe = Descriptor(
    ref = "3e5957f5-9b8f-4d36-871e-3ca73863ca7b",
    describes = SinkDescriptor(
      name = classOf[DiscardSinkLogic].getName,
      displayName = TextRef("displayName"),
      description = TextRef("description"),
      categories = Seq(CommonCategories.SINK, CommonCategories.DEBUG),
      parameters = Seq(intervalParameter),
      icon = Icon.fromClass(classOf[DiscardSinkLogic]),
      maturity = Maturity.Official
    ),
    localization = Localization.fromResourceBundle(
      bundleName = "io.logbee.keyscore.pipeline.contrib.DiscardSinkLogic",
      Locale.ENGLISH, Locale.GERMAN) ++ CATEGORY_LOCALIZATION
  )
}

class DiscardSinkLogic(parameters: LogicParameters, shape: SinkShape[Dataset]) extends SinkLogic(parameters, shape) {

  private var interval: FiniteDuration = 0 seconds

  private val pullAsync: AsyncCallback[Unit] = getAsyncCallback(_ => {
    pull(in)
  })

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {

    interval = configuration.findValue(DiscardSinkLogic.intervalParameter)
      .map(value => value milliseconds)
      .getOrElse(interval)

    pull(in)
  }

  override def onPush(): Unit = {

    grab(in)

    materializer.scheduleOnce(interval, () => pullAsync.invoke(()))
  }
}
