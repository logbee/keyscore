package io.logbee.keyscore.model.util

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

object ToFiniteDuration {
  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration = Duration.fromNanos(d.toNanos)
}
