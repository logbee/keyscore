package io.logbee.keyscore.pipeline.contrib.tailin.read

sealed trait ReadMode

object ReadMode {

  case object Line extends ReadMode
  case object File extends ReadMode

  def fromString(value: String): ReadMode = value match {
    case "Line" => Line
    case "File" => File
    case _ => throw new IllegalArgumentException(s"Unknown ReadMode: '$value'. Possible values are: [$Line, $File].")
  }
}
