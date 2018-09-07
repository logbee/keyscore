package io.logbee.keyscore.model.pipeline

sealed trait FilterStatus

case object Unknown extends FilterStatus
case object Paused extends FilterStatus
case object Running extends FilterStatus
case object Drained extends FilterStatus
case object Ready extends FilterStatus
