package io.logbee.keyscore.model.util

import java.nio.charset.StandardCharsets

import scala.language.implicitConversions

object Hex {

  implicit def toHexable(bytes: Array[Byte]): Hexable = new Hexable(bytes)
  implicit def toHexable(string: String): Hexable = new Hexable(string.getBytes(StandardCharsets.UTF_8))

  class Hexable(bytes: Array[Byte]) {
    def toHex: String = bytes.map("%02x" format _).mkString
  }
}
