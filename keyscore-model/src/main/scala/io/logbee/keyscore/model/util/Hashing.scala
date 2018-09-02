package io.logbee.keyscore.model.util

import java.security.MessageDigest
import java.util.{Base64, UUID}

object Hashing {

  implicit def toHashable(data: String): Hashable = new Hashable(data)
  implicit def toHashable(data: Int): Hashable = new Hashable(data.toString)
  implicit def toHashable(data: UUID): Hashable = new Hashable(data.toString)

  class Hashable(data: String) {
    def md5(): String = {
      MessageDigest.getInstance("MD5").digest(data.getBytes).map(_.toChar).mkString
    }

    def base64(): String = {
      Base64.getEncoder.encodeToString(data.getBytes)
    }
  }
}
