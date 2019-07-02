package io.logbee.keyscore.pipeline.contrib.decoder.json

import io.logbee.keyscore.model.data._
import org.json4s.JsonAST._
import org.json4s.native.JsonParser.parse

import scala.collection.mutable
import scala.util.{Success, Try}

object JsonDecoderUtil {

  def extract(node: JValue, path: List[String] = List.empty, fields: List[Field] = List.empty): List[Field] = {
    node match {
      case obj: JObject =>
        obj.obj.foldLeft(fields) {
          case (fields, (name, jValue)) =>
            extract(jValue, path :+ name, fields)
        }

      case JArray(elements) =>
        elements.zipWithIndex.foldLeft(fields) {
          case (fields, (jValue, index)) =>
            extract(jValue, path :+ index.toString, fields)
        }

      case JBool(value) =>
        fields :+ Field(path.mkString("."), BooleanValue(value))

      case JInt(value) =>
        fields :+ Field(path.mkString("."), NumberValue(value.toLong))

      case JLong(value) =>
        fields :+ Field(path.mkString("."), NumberValue(value.toLong))

      case JDouble(value) =>
        fields :+ Field(path.mkString("."), DecimalValue(value))

      case JDecimal(value) =>
        fields :+ Field(path.mkString("."), DecimalValue(value.toDouble))

      case JString(value) =>
        fields :+ Field(path.mkString("."), TextValue(value))

      case _ =>
        fields
    }
  }

  def extract(value: String, prefix: String): Set[Field] = {

    val candidates = value
      .zipWithIndex
      .foldRight((mutable.Stack[(Int, Int, Int)](), mutable.Stack[Int]())) {

        case (('}', index), result @ (_, stack)) =>
          stack.push(index + 1)
          result

        case (('{', index), (candidates, stack)) if stack.nonEmpty =>
          candidates.push((index, stack.pop(), stack.size))
          (candidates, stack)

        case (_, result) => result
      }
      ._1

    val depthCount = mutable.HashMap.empty[Int, Int]
    val fields = mutable.HashMap.empty[String, Field]

    while(candidates.nonEmpty) {
      val (start, end, depth) = candidates.pop()
      Try(parse(value.substring(start, end))) match {
        case Success(root) =>
          val index = depthCount.getOrElse(depth, 0)
          extract(root, List(prefix, index.toString) , List.empty).foreach(field => fields.put(field.name, field))
          depthCount.put(depth, index + 1)
          removeSubs(depth, candidates)

        case _ =>
      }
    }

    fields.values.toSet
  }

  private def removeSubs(depth: Int, stack: mutable.Stack[(Int, Int, Int)]): Unit = {
    while (stack.nonEmpty) {
      if (stack.top._3 <= depth) return
      stack.pop()
    }
  }
}
