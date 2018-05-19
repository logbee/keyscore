package io.logbee.keyscore.commons.cluster

import java.util.UUID

import akka.actor.ActorRef
import akka.cluster.Member
import io.logbee.keyscore.model.StreamConfiguration
import io.logbee.keyscore.model.filter.MetaFilterDescriptor

case class AgentJoin(id: UUID, name: String)
case class AgentJoinAccepted()
case class AgentJoinFailure(cause: Int)

case class AgentJoined(ref: ActorRef)
case class AgentLeaved(ref: ActorRef)

case class AgentCapabilities(filterDescriptors: List[MetaFilterDescriptor])

case class MemberAdded(member: Member)
case class MemberRemoved(member: Member)

case class CreateNewStream(streamID: UUID, stream: StreamConfiguration)

case class StreamKilled(streamID: UUID)

case class GraphCreated(streamID: UUID)
case class GraphBuildingException(streamID: UUID, streamSpec: StreamConfiguration, errorMsg: String)