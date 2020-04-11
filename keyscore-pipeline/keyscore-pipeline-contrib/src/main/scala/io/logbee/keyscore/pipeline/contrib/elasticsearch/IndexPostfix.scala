package io.logbee.keyscore.pipeline.contrib.elasticsearch

sealed trait IndexPostfix

object IndexPostfix {
  case object None extends IndexPostfix
  case object Day extends IndexPostfix
  case object Month extends IndexPostfix
  
  def fromString(value: String): IndexPostfix = value.toLowerCase match {
    case "none" => None
    case "day" => Day
    case "month" => Month
    case _ => throw new IllegalArgumentException(s"Unknown ${getClass.getSimpleName}. Possible values are: [$None, $Day, $Month].")
  }
}
