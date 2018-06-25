package io.logbee.keyscore.model

import io.logbee.keyscore.model.NativeModel.NativeField


object Field {

  implicit def fieldToNative(field: Field[_]): NativeField = {
    val builder = NativeField.newBuilder
    builder.setName(field.name)
    builder.setKind(field.kind)
    field match {
      case TextField(_, text) => builder.setText(text)
      case NumberField(_, number) => builder.setNumber(number)
      case TimestampField(_, timestamp) => builder.setTimestamp(timestamp)
    }
    builder.build()
  }

  implicit def fieldFromNative[A <: Field[_]](native: NativeField): A = {
    native.getKind match {
      case "text" => TextField(native.getName, native.getText).asInstanceOf[A]
      case "number" => NumberField(native.getName, native.getNumber).asInstanceOf[A]
      case "timestamp" => TimestampField(native.getName, native.getTimestamp).asInstanceOf[A]
      case _ => throw new IllegalArgumentException(s"Could not convert NativeField due to unknown kind: '${native.getKind}'")
    }
  }
}

trait Field[+T] {
  val name: String
  val kind: String
  val value: T
}

case class TextField(name: String, value: String) extends Field[String] {
  override val kind: String = "text"
}

case class NumberField(name: String, value: Double) extends Field[Double] {
  override val kind: String = "number"
}

case class TimestampField(name: String, value: Long) extends Field[Long] {
  override val kind: String = "timestamp"
}
