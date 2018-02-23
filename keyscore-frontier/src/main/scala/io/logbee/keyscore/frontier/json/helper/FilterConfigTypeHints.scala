package io.logbee.keyscore.frontier.json.helper

import io.logbee.keyscore.model.filter._
import org.json4s.JsonAST.JObject
import org.json4s.{CustomSerializer, TypeHints}

object FilterConfigTypeHints extends TypeHints {
  val classToHint: Map[Class[_], String] = Map(
    classOf[TextParameter] -> "string",
    classOf[BooleanParameter] -> "boolean",
    classOf[IntParameter] -> "int",
    classOf[FloatParameter] -> "float",
    classOf[TextMapParameter] -> "map[string,string]",
    classOf[TextListParameter] -> "list[string]"
  )

  val hintToClass = classToHint.map(_.swap)

  override val hints: List[Class[_]] = List(
    classOf[TextParameter],
    classOf[BooleanParameter],
    classOf[IntParameter],
    classOf[FloatParameter],
    classOf[TextMapParameter],
    classOf[TextListParameter],
    classOf[String]
  )

  override def classFor(hint: String): Option[Class[_]] = hintToClass.get(hint)

  override def hintFor(clazz: Class[_]): String = classToHint(clazz)
}

/*class ParameterSerializer extends CustomSerializer[Parameter[_]](format =>({
  case jsonObj:JObject =>
    val kind = (jsonObj \ "kind").extract[String]
    kind match{
      case "string" =>
    }

}))*/
