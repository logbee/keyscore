package io.logbee.keyscore.model

import java.util.UUID

case class StreamBlueprint(id: UUID, filters: Seq[FilterBlueprint])
