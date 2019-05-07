package io.logbee.keyscore.model

import scala.language.implicitConversions

trait HashCompanion {
  implicit def hashToString(hash: Hash): String = hash.value
  implicit def stringToHash(string: String): Hash = Hash(string)
}

trait BaseHash {
  this: Hash =>
  def nonEmpty: Boolean = value.nonEmpty
}