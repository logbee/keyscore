package io.logbee.keyscore.model

import scalapb.TypeMapper

trait ParameterRefCompanion {
  implicit val typeMapper = TypeMapper[String, ParameterRef](ParameterRef.apply)(_.ref)
}
