package io.logbee.keyscore.pipeline.contrib.tailin.read

sealed trait ReadMode

object ReadMode {

  case object Line extends ReadMode
  case object MultiLine extends ReadMode
  case object File extends ReadMode

  def fromString(value: String): ReadMode = value.toLowerCase match {
    case "line" => Line
    case "multiline" => MultiLine
    case "file" => File
    case _ => throw new IllegalArgumentException(s"Unknown ReadMode: '$value'. Possible values are: [$Line, $MultiLine, $File].")
  }
}
