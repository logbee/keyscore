package io.logbee.keyscore.model.util

object ToOption {
  implicit def T2OptionT[T](x: T) : Option[T] = Option(x)
}
