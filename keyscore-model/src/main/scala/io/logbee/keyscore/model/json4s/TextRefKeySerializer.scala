package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.localization.TextRef
import org.json4s.CustomKeySerializer

object TextRefKeySerializer extends CustomKeySerializer[TextRef](format => ({
  case id: String => TextRef(id)
}, {
  case ref: TextRef => ref.id
}))