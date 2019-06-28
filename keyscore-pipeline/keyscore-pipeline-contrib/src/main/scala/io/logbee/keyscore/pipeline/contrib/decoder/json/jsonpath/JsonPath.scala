package io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath

import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.JsonPath.ParserLike
import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.Tokenizer.{IndexTokenizer, NodeTokenizer, RootTokenizer, WildcardTokenizer}

import scala.annotation.tailrec
import scala.language.implicitConversions


object JsonPath {

  case class JsonPathParserException(message: String) extends RuntimeException(message)

  trait ParserLike[T] {
    def parse(in: T, jsonpath: JsonPath): T
  }

  def apply(jsonpath: String): JsonPath = {
    @tailrec def evaluate(tokens: List[Token], tail: String): List[Token] = tail match {
        case RootTokenizer(token, tail) => evaluate(tokens :+ token, tail)
        case WildcardTokenizer(token, tail) => evaluate(tokens :+ token, tail)
        case NodeTokenizer(token, tail) => evaluate(tokens :+ token, tail)
        case IndexTokenizer(token, tail) => evaluate(tokens :+ token, tail)
        case _ => tokens
    }
    new JsonPath(evaluate(List.empty, jsonpath))
  }

  def apply(tokens: Token*): JsonPath = new JsonPath(tokens.toList)

  implicit def String2JsonPath(string: String): JsonPath = JsonPath(string)
}

class JsonPath(val tokens: List[Token]) {

  val path: String = tokens.map(_.token).mkString

  def parse[T](json: T)(implicit jsonLike: ParserLike[T]): T = {
    jsonLike.parse(json, this)
  }

  def canEqual(a: Any): Boolean = a.isInstanceOf[JsonPath]

  override def equals(other: Any): Boolean = other match {
    case that: JsonPath =>
      (that canEqual this) &&
        tokens == that.tokens
    case _ => false
  }

  override def hashCode(): Int = {
    Seq(tokens).map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
