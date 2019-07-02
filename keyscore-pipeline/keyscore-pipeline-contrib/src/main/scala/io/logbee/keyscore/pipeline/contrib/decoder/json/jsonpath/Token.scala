package io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath

trait Token {
  val token: String
}

object Token {

  case class RootToken() extends Token {
    val token: String = "$"
  }

  case class WildcardToken() extends Token {
    val token: String = ".*"
  }

  case class NodeToken(name: String) extends Token {
    val token = s".$name"
  }

  case class IndexToken(start: Option[Int], end: Option[Int]) extends Token {
    val token: String = s"[${start.getOrElse("")}:${end.getOrElse("")}]"
  }
}
