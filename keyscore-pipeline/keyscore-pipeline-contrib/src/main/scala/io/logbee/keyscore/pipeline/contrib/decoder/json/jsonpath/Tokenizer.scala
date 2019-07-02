package io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath

import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.Token.{IndexToken, NodeToken, RootToken, WildcardToken}

trait Tokenizer[T] {
  def unapply(string: String): Option[(T, String)]
}

object Tokenizer {

  object RootTokenizer extends Tokenizer[RootToken] {

    private val Pattern = "^(\\$)(.*)".r

    def unapply(string: String): Option[(RootToken, String)] = string match {
      case Pattern(_, tail) => Some((RootToken(), tail))
      case _ => None
    }
  }

  object WildcardTokenizer extends Tokenizer[WildcardToken] {

    private val Pattern = "^\\.(\\*)(.*)".r

    def unapply(string: String): Option[(WildcardToken, String)] = string match {
      case Pattern(_, tail) => Some((WildcardToken(), tail))
      case _ => None
    }
  }

  object NodeTokenizer extends Tokenizer[NodeToken] {

    private val Pattern = "^\\.(\\w*)(.*)".r

    def unapply(string: String): Option[(NodeToken, String)] = string match {
      case Pattern(name, tail) => Some(NodeToken(name), tail)
      case _ => None
    }
  }

  object IndexTokenizer extends Tokenizer[IndexToken] {

    private val Pattern = "^\\[([+-]?\\d*):([+-]?\\d*)\\](.*)".r

    def unapply(string: String): Option[(IndexToken, String)] = string match {
      case Pattern(start, "", tail) => Some((IndexToken(Option(start.toInt), None), tail))
      case Pattern("", end, tail) => Some((IndexToken(None, Option(end.toInt)), tail))
      case Pattern(start, end, tail) => Some((IndexToken(Option(start.toInt), Option(end.toInt)), tail))
      case _ => None
    }
  }
}