package io.logbee.keyscore.model.blueprint

import java.util.UUID
import scala.language.implicitConversions

trait BlueprintRefCompanion {

  implicit def uuidFromBlueprintRef(ref: BlueprintRef): UUID = UUID.fromString(ref.uuid)
  implicit def stringToBlueprintRef(uuid: String): BlueprintRef = BlueprintRef(uuid)

}
