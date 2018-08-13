package io.logbee.keyscore.model

import scalapb.TypeMapper

trait TextRefCompanion {
  implicit val typeMapper = TypeMapper[String, TextRef](TextRef.apply)(_.ref)
}
