package io.logbee.keyscore.commons.serviceregistry.internal

import akka.actor.{Actor, ActorLogging}
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.Registrar._
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.ServiceKey

import scala.collection.mutable

class LocalServiceRegistryActor extends Actor with ActorLogging {

  import akka.actor.typed
  import akka.actor.typed.scaladsl.adapter._

  private val services = mutable.HashMap.empty[ServiceKey[Any], mutable.Set[typed.ActorRef[Any]]]

  override def receive: Receive = {

    case Register(key, service, sender) =>
      val registeredServices = services.getOrElseUpdate(key, mutable.Set.empty)
      registeredServices.add(service)
      sender.foreach(_ ! Registered(key, service, self))
      log.debug("Registered service '{}' for key '{}': {}", service, key, registeredServices.mkString("[", ", ", "]"))

    case Unregister(key, service, sender) =>
      val registeredServices = services.getOrElse(key, mutable.Set.empty)
      registeredServices.remove(service)
      sender.foreach(_ ! Unregistered(key, service, self))
      log.debug("Unregistered service '{}' for key '{}': {}", service, key, registeredServices.mkString)

    case Subscribe(key, subscriber) =>
      subscriber ! Subscribed(key, self)
      log.debug("'{}' has subscribed to '{}'.", subscriber, key)

    case Unsubscribe(key, subscriber) =>
      subscriber ! Unsubscribed(key, self)
      log.debug("'{}' has unsubscribed from '{}'.", subscriber, key)

    case Find(key, sender) =>
      sender ! Listing(key, services.getOrElse(key, mutable.Set.empty).toSet, self)
  }
}

object LocalServiceRegistryActor
