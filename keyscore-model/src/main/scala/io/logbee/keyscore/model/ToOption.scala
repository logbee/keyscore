package io.logbee.keyscore.model

object ToOption {
  implicit def T2OptionT[T](x: T) : Option[T] = Option(x)
}

