package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.actor.ActorRef


case class RemoteAgent(id: UUID, name: String, memberId: Long, ref: ActorRef)