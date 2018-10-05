package io.logbee.keyscore.model.util

object Hex {

  implicit def toHexable(bytes: Array[Byte]): Hexable = new Hexable(bytes)

  class Hexable(bytes: Array[Byte]) {
    def toHex: String = bytes.map("%02x" format _).mkString
  }
}
