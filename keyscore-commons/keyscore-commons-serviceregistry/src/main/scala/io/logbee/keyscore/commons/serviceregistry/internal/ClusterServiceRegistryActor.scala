package io.logbee.keyscore.commons.serviceregistry.internal

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.{Actor, ActorLogging, ActorRef, typed}
import akka.cluster.ddata.Replicator.{GetResponse, UpdateResponse}
import akka.cluster.ddata._
import com.typesafe.config.Config
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.Registrar._
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.ServiceKey

import scala.language.implicitConversions

class ClusterServiceRegistryActor extends Actor with ActorLogging {

  import ClusterServiceRegistryActor.Configuration
  import akka.actor.typed.scaladsl.adapter._
  import context.system

  type ActorRefType = typed.ActorRef[Any]

  private val configuration = Configuration(context.system.settings.config)

  private implicit val node: SelfUniqueAddress = DistributedData(system).selfUniqueAddress
  private val replicator: ActorRef = DistributedData(context.system).replicator

  override def receive: Receive = {

    case Register(key, service, replyTo) =>

      context.spawnAnonymous(Behaviors.setup[UpdateResponse[ORSet[ActorRefType]]] { context =>

        replicator tell (Replicator.Update(key, ORSet.empty[ActorRefType], configuration.writeConsistency) { services =>
          services :+ service
        }, context.self.toUntyped)

        Behaviors.receiveMessage {

          case _: Replicator.UpdateSuccess[_] =>
            replyTo.foreach(_ ! Registered(key, service, self))
            log.debug("Registered service '{}' for key '{}'", service, key)
            Behaviors.stopped

          case _ =>
            log.warning("Failed to register service '{}' for key '{}'.", service, key)
            Behaviors.stopped
        }
      })

    case Unregister(key, service, replyTo) =>

      context.spawnAnonymous(Behaviors.setup[UpdateResponse[ORSet[ActorRefType]]] { context =>

        replicator tell (Replicator.Update(key, ORSet.empty[ActorRefType], configuration.writeConsistency) { services =>
          services.remove(service.toUntyped)
        }, context.self.toUntyped)

        Behaviors.receiveMessage {

          case _: Replicator.UpdateSuccess[_] =>
            replyTo.foreach(_ ! Unregistered(key, service, self))
            log.debug("Unregistered service '{}' from key '{}'.", service, key)
            Behaviors.stopped

          case _ =>
            log.warning("Failed to unregister service '{}' from key '{}'.", service, key)
            Behaviors.stopped
        }
      })

    case Subscribe(key, subscriber) =>
      subscriber ! Subscribed(key, self)
      log.debug("'{}' has subscribed to '{}'.", subscriber, key)

    case Unsubscribe(key, subscriber) =>
      subscriber ! Unsubscribed(key, self)
      log.debug("'{}' has unsubscribed from '{}'.", subscriber, key)

    case Find(key, repayTo) =>

      context.spawnAnonymous(Behaviors.setup[GetResponse[ORSet[ActorRefType]]] { context =>

        replicator tell (Replicator.Get(key, configuration.readConsistency), context.self.toUntyped)

        Behaviors.receiveMessage {

          case response: Replicator.GetSuccess[_] =>
            val services = response.get(key)
            repayTo ! Listing(key, services.elements, self)
            Behaviors.stopped

          case _ =>
            log.warning("Failed to find services for key '{}'.", key)
            Behaviors.stopped
        }
      })
  }

  private implicit def toKey(key: ServiceKey[Any]): ORSetKey[ActorRefType] = ORSetKey[ActorRefType](s"${configuration.keyPrefix}${key.id}")
}

object ClusterServiceRegistryActor {

  private object Configuration {

    import io.logbee.keyscore.model.util.ToFiniteDuration.asFiniteDuration

    def apply(config: Config): Configuration = {

      val registryConfig = config.getConfig("keyscore.service-registry")
      val clusterConfig = registryConfig.getConfig("cluster")
      val replicationConfig = clusterConfig.getConfig("replication")

      val readTimeout = replicationConfig.getDuration("read-timeout")
      val writeTimeout = replicationConfig.getDuration("write-timeout")

      Configuration(
        readConsistency = replicationConfig.getString("read-consistency") match {
          case "local" => Replicator.ReadLocal
          case "majority" => Replicator.ReadMajority(readTimeout)
          case "all" => Replicator.ReadAll(readTimeout)
        },
        writeConsistency = replicationConfig.getString("write-consistency") match {
          case "local" => Replicator.WriteLocal
          case "majority" => Replicator.WriteMajority(writeTimeout)
          case "all" => Replicator.WriteAll(writeTimeout)
        },
        keyPrefix = registryConfig.getString("key-prefix")
      )
    }
  }

  private case class Configuration(
    readConsistency: Replicator.ReadConsistency,
    writeConsistency: Replicator.WriteConsistency,
    keyPrefix: String
  )
}
