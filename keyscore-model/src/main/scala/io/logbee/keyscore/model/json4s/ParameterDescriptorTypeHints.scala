package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.filter._
import org.json4s.TypeHints

object ParameterDescriptorTypeHints extends TypeHints {
  val classToHint: Map[Class[_], String] = Map(
    classOf[BooleanParameterDescriptor] -> "boolean",
    classOf[TextParameterDescriptor] -> "text",
    classOf[IntParameterDescriptor] -> "int",
    classOf[ListParameterDescriptor] -> "list",
    classOf[MapParameterDescriptor] -> "map"
  )

  val hintToClass = classToHint.map(_.swap)

  override val hints: List[Class[_]] = List(
    classOf[BooleanParameterDescriptor],
    classOf[TextParameterDescriptor],
    classOf[IntParameterDescriptor],
    classOf[ListParameterDescriptor],
    classOf[MapParameterDescriptor]

  )

  override def classFor(hint: String): Option[Class[_]] = hintToClass.get(hint)

  override def hintFor(clazz: Class[_]): String = classToHint(clazz)
}

