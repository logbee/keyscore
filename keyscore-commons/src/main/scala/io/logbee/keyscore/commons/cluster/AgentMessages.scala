package io.logbee.keyscore.commons.cluster

import java.util.{Locale, UUID}

import akka.actor.ActorRef
import akka.cluster.Member
import io.logbee.keyscore.model.StreamModel
import io.logbee.keyscore.model.sink.FilterDescriptor

import scala.collection.mutable

case class AgentJoin(id: UUID, name: String)
case class AgentJoinAccepted()
case class AgentJoinFailure(cause: Int)

case class AgentJoined(ref: ActorRef)
case class AgentLeaved(ref: ActorRef)

case class AgentCapabilities(filterDescriptors: List[mutable.Map[Locale,FilterDescriptor]])

case class MemberAdded(member: Member)
case class MemberRemoved(member: Member)

case class CreateNewStream(streamID: UUID, stream: StreamModel)

case class StreamKilled(streamID: UUID)

case class GraphCreated(streamID: UUID)
case class GraphBuildingException(streamID: UUID, streamSpec: StreamModel, errorMsg: String)