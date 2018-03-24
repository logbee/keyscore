package io.logbee.keyscore.commons.cluster

import io.logbee.keyscore.model.filter.FilterDescriptor

case class AgentJoin(name: String)
case class AgentJoinAccepted()
case class AgentJoinFailure(cause: Int)

case class AgentCapabilities(filterDescriptors: List[FilterDescriptor])