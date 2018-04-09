package io.logbee.keyscore.frontier.cluster

import akka.actor.ActorRef


case class RemoteAgent(name: String, memberId: Long, ref: ActorRef)