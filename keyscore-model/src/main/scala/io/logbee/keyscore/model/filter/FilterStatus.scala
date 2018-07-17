package io.logbee.keyscore.model.filter

sealed trait FilterStatus

case object Unknown extends FilterStatus
case object Paused extends FilterStatus
case object Running extends FilterStatus
case object Drained extends FilterStatus
