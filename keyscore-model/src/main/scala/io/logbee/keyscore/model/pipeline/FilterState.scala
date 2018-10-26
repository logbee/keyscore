package io.logbee.keyscore.model.pipeline

import java.util.UUID

import io.logbee.keyscore.model.data.Health

case class FilterState(id:UUID, health: Health, throughPutTime: Long = 0, totalThroughputTime: Long = 0, status: FilterStatus = Unknown)
