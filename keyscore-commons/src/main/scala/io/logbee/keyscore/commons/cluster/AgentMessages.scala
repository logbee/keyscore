package io.logbee.keyscore.commons.cluster

import java.util.UUID

import akka.actor.ActorRef
import akka.cluster.Member
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.blueprint.PipelineBlueprint
import io.logbee.keyscore.model.descriptor.Descriptor

case class AgentJoin(id: UUID, name: String)
case class AgentJoinAccepted()
case class AgentJoinFailure(cause: Int)

case class AgentJoined(ref: ActorRef)
case class AgentLeaved(ref: ActorRef)

case class AgentCapabilities(filterDescriptors: List[Descriptor])

case class MemberAdded(member: Member)
case class MemberRemoved(member: Member)

case class CreateNewStream(streamID: UUID, stream: PipelineConfiguration)

case class StreamKilled(streamID: UUID)

case class GraphCreated(streamID: UUID)
case class GraphBuildingException(streamID: UUID, streamSpec: PipelineConfiguration, errorMsg: String)

case class CreatePipelineOrder(blueprint: PipelineBlueprint)
case class DeletePipelineOrder(id: UUID)

case object DeleteAllPipelinesOrder

case class RemoveAgentFromCluster(agentID: UUID)

case class AgentRemovedFromCluster(agentID: UUID)

case object RemoveAgentFromClusterFailed

case class MemberJoin(obj: String, member: Member)
case class MemberLeave(obj: String, member: Member)
case class ActorJoin(obj: String, actorRef: ActorRef)
case class ActorLeave(obj: String, actorRef: ActorRef)
