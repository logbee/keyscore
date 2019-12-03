package io.logbee.keyscore.model.localization

import scalapb.TypeMapper

import scala.language.implicitConversions

trait TextRefCompanion {
  implicit val typeMapper = TypeMapper[String, TextRef](TextRef.apply)(_.id)
}
