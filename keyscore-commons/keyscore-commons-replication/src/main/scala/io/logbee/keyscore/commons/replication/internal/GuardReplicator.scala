package io.logbee.keyscore.commons.replication.internal

import akka.actor.{Actor, ActorLogging, Props, typed}
import io.logbee.keyscore.commons.replication.Realm
import io.logbee.keyscore.commons.replication.Replicator.Command
import io.logbee.keyscore.commons.replication.internal.GuardReplicator.{Lookup, LookupResult}

import scala.util.{Success, Try}

object GuardReplicator {

  case class Lookup(realm: Realm)

  case class LookupResult(result: Try[typed.ActorRef[Command]])
}

class GuardReplicator extends Actor with ActorLogging {

  import akka.actor.typed.scaladsl.adapter._

  override def receive: Receive = {
    case Lookup(realm) => sender ! LookupResult(Success(context.actorOf(Props(new Replicator()), s"${realm.realm}-replicator").toTyped))
  }
}
