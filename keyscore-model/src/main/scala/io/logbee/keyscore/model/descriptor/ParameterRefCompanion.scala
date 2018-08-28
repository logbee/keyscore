package io.logbee.keyscore.model.descriptor

import scalapb.TypeMapper

trait ParameterRefCompanion {
  implicit def parameterRefFromString(ref: String): ParameterRef = ParameterRef(ref)
  implicit val typeMapper = TypeMapper[String, ParameterRef](ParameterRef.apply)(_.id)
}
