package io.logbee.keyscore.commons.replication

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props, typed}
import akka.util.Timeout
import io.logbee.keyscore.commons.replication.Replicator.Command
import io.logbee.keyscore.commons.replication.internal.GuardReplicator
import io.logbee.keyscore.commons.replication.internal.GuardReplicator.{Lookup, LookupResult}

import scala.concurrent.Await
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class Replication(system: ExtendedActorSystem) extends Extension {

  import akka.pattern._

  private implicit val lookupTimeout: Timeout = Timeout.create(system.settings.config.getDuration("keyscore.replication.lookup-timeout"))

  private lazy val guardReplicatorActor = {
      system.log.info("Replication: Creating guard replicator.")
      system.systemActorOf(Props(new GuardReplicator()), "guard-replicator")
  }

  def replicator(realm: Realm): Try[typed.ActorRef[Command]] = {
    lazy val lookupFailureMessage = s"Failed to lookup replicator for realm '${realm.realm}' within ${lookupTimeout.duration}!"
    Try(Await.result((guardReplicatorActor ? Lookup(realm)).mapTo[LookupResult], lookupTimeout.duration)) match {
      case Success(LookupResult(result: Success[_])) => result
      case Success(LookupResult(Failure(exception))) =>
        Failure(ReplicatorLookupException(lookupFailureMessage, exception))
      case _ =>
        Failure(ReplicatorLookupException(lookupFailureMessage))
    }
  }
}

object Replication extends ExtensionId[Replication] with ExtensionIdProvider {

  override def lookup(): ExtensionId[_ <: Extension] = Replication

  override def createExtension(system: ExtendedActorSystem): Replication = new Replication(system)
}
