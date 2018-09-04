package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.ParameterRef
import org.json4s.CustomKeySerializer

object ParameterRefKeySerializer extends CustomKeySerializer[ParameterRef](format => ({
  case id: String => ParameterRef(id)
}, {
  case ref: ParameterRef => ref.id
}))