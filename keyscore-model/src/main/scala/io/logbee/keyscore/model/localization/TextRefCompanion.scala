package io.logbee.keyscore.model.localization

import scalapb.TypeMapper

trait TextRefCompanion {
  implicit def textRefFromString(ref: String): TextRef = TextRef(ref)
  implicit val typeMapper = TypeMapper[String, TextRef](TextRef.apply)(_.id)
}
