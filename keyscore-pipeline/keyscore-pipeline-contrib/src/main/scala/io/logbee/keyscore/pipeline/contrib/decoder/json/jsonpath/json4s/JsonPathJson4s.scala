package io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.json4s

import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.JsonPath.{JsonPathParserException, ParserLike, Selectable}
import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.{JsonPath, Token}
import io.logbee.keyscore.pipeline.contrib.decoder.json.jsonpath.Token.{IndexToken, NodeToken, RootToken, WildcardToken}
import org.json4s.JsonAST.{JArray, JObject, JValue}

import scala.annotation.tailrec
import scala.language.implicitConversions

object JsonPathJson4s {

  implicit object Parser extends ParserLike[JValue] {
    override def parse(in: JValue, jsonpath: JsonPath): JValue = {
      @tailrec def parse(in: JValue, tokens: List[Token]): JValue = {
        (in, tokens) match {
          case (jvalue: JValue, Nil) =>
            jvalue

          case (jvalue: JValue, RootToken() :: tail) =>
            parse(jvalue, tail)

          case (JObject(elements), WildcardToken() :: tail) =>
            parse(JArray(elements.map(_._2)), tail)

          case (JArray(array), IndexToken(Some(start), None) :: tail) if start < 0 =>
            parse(array(array.size + start), tail)

          case (JArray(array), IndexToken(Some(start), None) :: tail) =>
            parse(array(start), tail)

          case (JArray(array), IndexToken(None, Some(end)) :: tail) =>
            parse(JArray(array.slice(0, end + 1)), tail)

          case (JArray(array), IndexToken(Some(start), Some(end)) :: tail) =>
            parse(JArray(array.slice(start, end + 1)), tail)

          case (JArray(array), NodeToken(name) :: tail) =>
            parse(JArray(array.foldLeft(List.empty[JValue]) {
              case (result, JObject(obj)) => result ++ obj.filter(kv => name == kv._1).map(_._2)
              case (result, _) => result
            }), tail)

          case (jvalue: JValue, NodeToken(name) :: tail) =>
            parse(jvalue \ name, tail)

          case _ => throw JsonPathParserException(s"Failed to parse: '$in'")
        }
      }

      parse(in, jsonpath.tokens)
    }
  }

  implicit def JValue2Selectable(value: JValue): Selectable[JValue] = (jsonPath: JsonPath) => jsonPath.parse(value)
}
