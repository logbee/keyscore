package io.logbee.keyscore.model.util

import com.google.protobuf.any.Any
import scalapb.{GeneratedMessage, Message}

import scala.language.implicitConversions

object ToAny {
  implicit def Message2Any[T <: GeneratedMessage with Message[T]](t: T): Any = Any.pack(generatedMessage = t)
}
