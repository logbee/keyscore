package io.logbee.keyscore.model.localization

import io.logbee.keyscore.model.localization.TextRef
import scalapb.TypeMapper

trait TextRefCompanion {

  implicit def stringToTextRef(id: String): TextRef = TextRef(id)

  implicit val typeMapper = TypeMapper[String, TextRef](TextRef.apply)(_.id)
}
