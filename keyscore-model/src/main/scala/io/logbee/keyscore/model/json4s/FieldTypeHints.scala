package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.{NumberField, TextField, TimestampField}
import org.json4s.{ShortTypeHints, TypeHints}


object FieldTypeHints extends TypeHints {
val classToHint: Map[Class[_], String] = Map(
  classOf[TextField] -> "string",
  classOf[NumberField] -> "int",
  classOf[TimestampField] -> "timestamp"
)

  val hintToClass = classToHint.map(_.swap)

  override  val hints: List[Class[_]] = List(
    classOf[TextField],
    classOf[NumberField],
    classOf[TimestampField]
  )

  override def classFor(hint: String): Option[Class[_]] = hintToClass.get(hint)

  override def hintFor(clazz: Class[_]): String = classToHint(clazz)

  override def +(hints: TypeHints): TypeHints = ShortTypeHints(List.empty) + hints
}
