package io.logbee.keyscore.model.blueprint

import java.util.UUID

object ToBlueprintRef {
  implicit def stringToBlueprintRef(uuid: String): BlueprintRef = uuidToBlueprintRef(UUID.fromString(uuid))
  implicit def uuidToBlueprintRef(uuid: UUID): BlueprintRef = BlueprintRef(uuid.toString)
}
