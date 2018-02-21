package io.logbee.keyscore.commons.cluster

case class AgentJoin(name: String)
case class AgentJoinAccepted()
case class AgentJoinFailure(cause: Int)