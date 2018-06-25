package io.logbee.keyscore.model

sealed trait Health

case object Green extends Health
case object Red extends Health
case object Yellow extends Health