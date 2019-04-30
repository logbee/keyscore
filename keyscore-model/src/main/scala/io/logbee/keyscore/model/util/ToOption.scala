package io.logbee.keyscore.model.util

import scala.language.implicitConversions

object ToOption {
  implicit def T2OptionT[T](x: T) : Option[T] = Option(x)
}
