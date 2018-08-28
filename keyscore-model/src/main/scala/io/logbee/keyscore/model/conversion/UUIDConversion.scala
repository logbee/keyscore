package io.logbee.keyscore.model.conversion

import java.util.UUID

object UUIDConversion {

  implicit def uuidToString(uuid: UUID): String = uuid.toString

  implicit def uuidFromString(uuid: String): UUID = UUID.fromString(uuid)
}
