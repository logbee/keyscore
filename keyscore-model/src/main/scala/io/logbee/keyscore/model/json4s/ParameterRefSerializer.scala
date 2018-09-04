package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.ParameterRef
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object ParameterRefSerializer extends CustomSerializer[ParameterRef](format => ( {
  case JString(ref) => ParameterRef(ref)
  case JNull => null
}, {
  case ref: ParameterRef =>
    JString(ref.id)
}))