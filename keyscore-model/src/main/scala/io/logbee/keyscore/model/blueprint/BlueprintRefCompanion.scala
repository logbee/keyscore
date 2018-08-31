package io.logbee.keyscore.model.blueprint

import java.util.UUID

trait BlueprintRefCompanion {

  implicit def uuidFromBlueprintRef(ref: BlueprintRef): UUID = UUID.fromString(ref.uuid)
  implicit def stringToBlueprintRef(uuid: String): BlueprintRef = BlueprintRef(uuid)

}
