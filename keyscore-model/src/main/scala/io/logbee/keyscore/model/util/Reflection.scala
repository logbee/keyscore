package io.logbee.keyscore.model.util

import scala.language.implicitConversions

object Reflection {

  implicit def Object2ExtendedObject(obj: AnyRef): ExtendedObject = {
    new ExtendedObject(obj.getClass)
  }

  private val simpleClassName = """[\$\.]?(\w+)$""".r.unanchored

  class ExtendedObject(wrapped: Class[_]) {
    def getSimpleClassName: String = wrapped.getName match  {
      case simpleClassName(name) => name
      case _ => wrapped.getName
    }
  }
}
