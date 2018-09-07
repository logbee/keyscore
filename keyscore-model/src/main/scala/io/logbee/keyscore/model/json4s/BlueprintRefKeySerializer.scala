package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.blueprint.BlueprintRef
import org.json4s.CustomKeySerializer

object BlueprintRefKeySerializer extends CustomKeySerializer[BlueprintRef](format => ({
  case uuid: String => BlueprintRef(uuid)
}, {
  case ref: BlueprintRef => ref.uuid
}))
