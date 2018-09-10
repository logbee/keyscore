package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.ParameterRef
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JObject, JString}

case object ParameterRefSerializer extends CustomSerializer[ParameterRef](format => ( {
  case JObject(List(("id", JString(id)))) => ParameterRef(id)
  case JNull => null
}, {
  case ref: ParameterRef =>
    JObject(("id", JString(ref.id)))
}))