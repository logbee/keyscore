package io.logbee.keyscore.commons.serviceregistry

import akka.actor.typed.ActorRef
import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.Registrar.Command
import io.logbee.keyscore.commons.serviceregistry.internal.{ClusterServiceRegistryActor, LocalServiceRegistryActor}

import scala.language.{implicitConversions, postfixOps}
import scala.reflect.ClassTag

class ServiceRegistry(system: ExtendedActorSystem) extends Extension {

  import akka.actor.typed.scaladsl.adapter._

  private lazy val registryActor = {

    val provider = system.settings.ProviderClass

    if ("akka.cluster.ClusterActorRefProvider" == provider) {
      system.log.info("ServiceRegistry: Creating clustered registrar.")
      system.systemActorOf(Props(new ClusterServiceRegistryActor()), "cluster-service-registry")
    }
    else {
      system.log.info("ServiceRegistry: Creating local registrar.")
      system.systemActorOf(Props(new LocalServiceRegistryActor()), "local-service-registry")
    }
  }

  def registrar: ActorRef[Command] = registryActor
}

object ServiceRegistry extends ExtensionId[ServiceRegistry] with ExtensionIdProvider {

  override def lookup(): ExtensionId[_ <: Extension] = ServiceRegistry

  override def createExtension(system: ExtendedActorSystem): ServiceRegistry = new ServiceRegistry(system)

  object ServiceKey {

    def apply[T](id: String)(implicit classTag: ClassTag[T]) = new ServiceKey[T](id, classTag)
  }

  class ServiceKey[T] private(val id: String, private val classTag: ClassTag[T]) {

    def canEqual(other: Any): Boolean = other.isInstanceOf[ServiceKey[T]]

    override def equals(other: Any): Boolean = other match {
      case that: ServiceKey[T] => (that canEqual this) && id == that.id
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(id, classTag)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }

    override def toString = s"ServiceKey($id, $classTag)"
  }

  object Registrar {

    sealed trait Message extends Serializable

    sealed trait Command extends Message

    sealed trait Event extends Message

    case class Register[T](key: ServiceKey[T], service: ActorRef[T], replyTo: Option[ActorRef[Registered[T]]] = None) extends Command

    case class Registered[T](key: ServiceKey[T], service: ActorRef[T], replyTo: ActorRef[Command]) extends Event

    case class Unregister[T](key: ServiceKey[T], service: ActorRef[T], replyTo: Option[ActorRef[Unregistered[T]]] = None) extends Command

    case class Unregistered[T](key: ServiceKey[T], service: ActorRef[T], replyTo: ActorRef[Command]) extends Event

    case class Subscribe[T](key: ServiceKey[T], subscriber: ActorRef[Subscribed[T]]) extends Command

    case class Subscribed[T](key: ServiceKey[T], replyTo: ActorRef[Command]) extends Event

    case class Unsubscribe[T](key: ServiceKey[T], subscriber: ActorRef[Unsubscribed[T]]) extends Command

    case class Unsubscribed[T](key: ServiceKey[T], replyTo: ActorRef[Command]) extends Event

    case class Find[T](key: ServiceKey[T], repayTo: ActorRef[Listing[T]]) extends Command

    case class Listing[T](key: ServiceKey[T], services: Set[ActorRef[T]], replyTo: ActorRef[Command]) extends Event
  }
}
