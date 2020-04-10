package io.logbee.keyscore.pipeline.contrib.tailin.read

import scala.util.matching.Regex

sealed trait ReadMode

object ReadMode {

  case object Line extends ReadMode
  case class MultiLine(firstLineRegex: Regex) extends ReadMode
  case class MultiLineWithEnd(firstLineRegex: Regex, lastLineRegex: Regex) extends ReadMode
  case object File extends ReadMode

  def fromStringParams(readModeString: String, firstLinePattern: String, lastLinePattern: String): ReadMode = readModeString.toLowerCase match {
    case "line" => Line
    case "multiline" => MultiLine(firstLinePattern.r)
    case "multilinewithend" => MultiLineWithEnd(firstLinePattern.r, lastLinePattern.r)
    case "file" => File
    case _ => throw new IllegalArgumentException(s"Unknown ReadMode: '$readModeString'. Possible values are: [$Line, $MultiLine, $MultiLineWithEnd, $File].")
  }
}
