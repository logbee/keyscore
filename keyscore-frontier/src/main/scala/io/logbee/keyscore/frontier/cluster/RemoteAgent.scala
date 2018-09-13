package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.actor.ActorRef

/**
  * Object to combine information from a member of the cluster with the information of the referring actor.
  */
case class RemoteAgent(id: UUID, name: String, memberId: Long, ref: ActorRef)