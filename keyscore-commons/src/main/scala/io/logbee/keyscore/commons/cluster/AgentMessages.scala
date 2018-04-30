package io.logbee.keyscore.commons.cluster

import java.util.UUID

import akka.actor.ActorRef
import akka.cluster.Member
import io.logbee.keyscore.model.filter.FilterDescriptor

case class AgentJoin(id: UUID, name: String)
case class AgentJoinAccepted()
case class AgentJoinFailure(cause: Int)

case class AgentJoined(ref: ActorRef)
case class AgentLeaved(ref: ActorRef)

case class AgentCapabilities(filterDescriptors: List[FilterDescriptor])

case class MemberAdded(member: Member)
case class MemberRemoved(member: Member)