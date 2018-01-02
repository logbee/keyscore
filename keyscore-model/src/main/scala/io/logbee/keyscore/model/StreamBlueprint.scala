package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.filter.FilterBlueprint

case class StreamBlueprint(id: UUID, filters: Seq[FilterBlueprint])
