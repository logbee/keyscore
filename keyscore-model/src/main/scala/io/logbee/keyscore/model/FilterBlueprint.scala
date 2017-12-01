package io.logbee.keyscore.model

import java.util.UUID

case class FilterBlueprint(filterType: String, id: UUID, parameters: Map[String, String])
