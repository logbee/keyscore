package io.logbee.keyscore.commons.serviceregistry.internal.proto

import java.io.NotSerializableException

import akka.actor.ExtendedActorSystem
import akka.serialization.SerializerWithStringManifest
import io.logbee.keyscore.commons.serialization.ActorRefSerialization
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.Registrar._
import io.logbee.keyscore.commons.serviceregistry.ServiceRegistry.ServiceKey

import scala.collection.immutable

class RegistrarMessageSerializer(val system: ExtendedActorSystem) extends SerializerWithStringManifest with ActorRefSerialization {

  private val manifests = immutable.HashMap[Class[_], String](
    classOf[Register[_]] -> "A",
    classOf[Registered[_]] -> "B",
    classOf[Unregister[_]] -> "D",
    classOf[Unregistered[_]] -> "C",
    classOf[Subscribe[_]] -> "E",
    classOf[Subscribed[_]] -> "F",
    classOf[Unsubscribe[_]] -> "G",
    classOf[Unsubscribed[_]] -> "H",
    classOf[Find[_]] -> "I",
    classOf[Listing[_]] -> "J",
  )

  private val fromBinaryMap = immutable.HashMap[String, Array[Byte] => Message](
    manifests(classOf[Register[_]]) -> fromBinaryRegister,
    manifests(classOf[Registered[_]]) -> fromBinaryRegistered,
    manifests(classOf[Unregister[_]]) -> fromBinaryUnregister,
    manifests(classOf[Unregistered[_]]) -> fromBinaryUnregistered,
    manifests(classOf[Subscribe[_]]) -> fromBinarySubscribe,
    manifests(classOf[Subscribed[_]]) -> fromBinarySubscribed,
    manifests(classOf[Unsubscribe[_]]) -> fromBinaryUnsubscribe,
    manifests(classOf[Unsubscribed[_]]) -> fromBinaryUnsubscribed,
    manifests(classOf[Find[_]]) -> FromBinaryFind,
    manifests(classOf[Listing[_]]) -> fromBinaryListing,
  )

  override def identifier: Int = -173421

  override def manifest(obj: AnyRef): String = manifests.get(obj.getClass) match {
    case Some(manifest) => manifest
    case _ => throw new IllegalArgumentException(s"Can't serialize object of type ${obj.getClass} in [${getClass.getName}]")
  }

  override def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case message: Register[_] => toBinaryRegister(message)
    case message: Registered[_] => toBinaryRegistered(message)
    case message: Unregister[_] => toBinaryUnregister(message)
    case message: Unregistered[_] => toBinaryUnregistered(message)
    case message: Subscribe[_] => toBinarySubscribe(message)
    case message: Subscribed[_] => toBinarySubscribed(message)
    case message: Unsubscribe[_] => toBinaryUnsubscribe(message)
    case message: Unsubscribed[_] => toBinaryUnsubscribed(message)
    case message: Find[_] => toBinaryFind(message)
    case message: Listing[_] => toBinaryListing(message)
    case _ => throw new IllegalArgumentException(s"Can't serialize object of type ${obj.getClass} in [${getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = fromBinaryMap.get(manifest) match {
    case Some(fromBinary) => fromBinary(bytes)
    case _ => throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
  }

  private def toBinaryRegister(message: Register[_]): Array[Byte] = RegisterMessage(
      key = message.key.id,
      service = serializeTypedActorRef(message.service),
      replyTo = serializeTypedActorRef(message.replyTo)
  ).toByteArray

  private def fromBinaryRegister(bytes: Array[Byte]): Register[_] = {
    val message = RegisterMessage.parseFrom(bytes)
    Register(
      key = ServiceKey[Any](message.key),
      service = deserializeTypedActorRef(message.service),
      replyTo = Option(deserializeTypedActorRef(message.replyTo))
    )
  }

  private def toBinaryRegistered(message: Registered[_]): Array[Byte] = RegisteredMessage(
    key = message.key.id,
    service = serializeTypedActorRef(message.service),
    replyTo = serializeTypedActorRef(message.replyTo)
  ).toByteArray

  private def fromBinaryRegistered(bytes: Array[Byte]): Registered[_] = {
    val message = RegisteredMessage.parseFrom(bytes)
    Registered(
      key = ServiceKey[Any](message.key),
      service = deserializeTypedActorRef(message.service),
      replyTo = deserializeTypedActorRef(message.replyTo)
    )
  }

  private def toBinaryUnregister(message: Unregister[_]): Array[Byte] = UnregisterMessage(
    key = message.key.id,
    service = serializeTypedActorRef(message.service),
    replyTo = serializeTypedActorRef(message.replyTo)
  ).toByteArray

  private def fromBinaryUnregister(bytes: Array[Byte]): Unregister[_] = {
    val message = UnregisterMessage.parseFrom(bytes)
    Unregister(
      key = ServiceKey[Any](message.key),
      service = deserializeTypedActorRef(message.service),
      replyTo = Option(deserializeTypedActorRef(message.replyTo))
    )
  }

  private def toBinaryUnregistered(message: Unregistered[_]): Array[Byte] = UnregisteredMessage(
    key = message.key.id,
    service = serializeTypedActorRef(message.service),
    replyTo = serializeTypedActorRef(message.replyTo)
  ).toByteArray

  private def fromBinaryUnregistered(bytes: Array[Byte]): Unregistered[_] = {
    val message = UnregisteredMessage.parseFrom(bytes)
    Unregistered(
      key = ServiceKey[Any](message.key),
      service = deserializeTypedActorRef(message.service),
      replyTo = deserializeTypedActorRef(message.replyTo)
    )
  }

  private def toBinarySubscribe(message: Subscribe[_]): Array[Byte] = SubscribeMessage(
    key = message.key.id,
    subscriber = serializeTypedActorRef(message.subscriber)
  ).toByteArray

  private def fromBinarySubscribe(bytes: Array[Byte]): Subscribe[_] = {
    val message = SubscribeMessage.parseFrom(bytes)
    Subscribe(
      key = ServiceKey[Any](message.key),
      subscriber = deserializeTypedActorRef[Subscribed[_]](message.subscriber)
    )
  }

  private def toBinarySubscribed(message: Subscribed[_]): Array[Byte] = SubscribedMessage(
    key = message.key.id,
    replyTo = serializeTypedActorRef(message.replyTo)
  ).toByteArray

  private def fromBinarySubscribed(bytes: Array[Byte]): Subscribed[_] = {
    val message = SubscribedMessage.parseFrom(bytes)
    Subscribed(
      key = ServiceKey[Any](message.key),
      replyTo = deserializeTypedActorRef(message.replyTo)
    )
  }

  private def toBinaryUnsubscribe(message: Unsubscribe[_]): Array[Byte] = UnsubscribeMessage(
    key = message.key.id,
    subscriber = serializeTypedActorRef(message.subscriber)
  ).toByteArray

  private def fromBinaryUnsubscribe(bytes: Array[Byte]): Unsubscribe[_] = {
    val message = UnsubscribeMessage.parseFrom(bytes)
    Unsubscribe(
      key = ServiceKey[Any](message.key),
      subscriber = deserializeTypedActorRef[Unsubscribed[_]](message.subscriber)
    )
  }

  private def toBinaryUnsubscribed(message: Unsubscribed[_]): Array[Byte] = UnsubscribedMessage(
    key = message.key.id,
    replyTo = serializeTypedActorRef(message.replyTo)
  ).toByteArray

  private def fromBinaryUnsubscribed(bytes: Array[Byte]): Unsubscribed[_] = {
    val message = UnsubscribedMessage.parseFrom(bytes)
    Unsubscribed(
      key = ServiceKey[Any](message.key),
      replyTo = deserializeTypedActorRef(message.replyTo)
    )
  }

  private def toBinaryFind(message: Find[_]): Array[Byte] = FindMessage(
    key = message.key.id,
    repayTo = serializeTypedActorRef(message.repayTo)
  ).toByteArray

  private def FromBinaryFind(bytes: Array[Byte]): Find[_] = {
    val message = FindMessage.parseFrom(bytes)
    Find(
      key = ServiceKey[Any](message.key),
      repayTo = deserializeTypedActorRef[Listing[_]](message.repayTo)
    )
  }

  private def toBinaryListing(message: Listing[_]): Array[Byte] = ListingMessage(
    key = message.key.id,
    services = message.services.map(serializeTypedActorRef).toSeq,
    replyTo = serializeTypedActorRef(message.replyTo)
  ).toByteArray

  private def fromBinaryListing(bytes: Array[Byte]): Listing[_] = {
    val message = ListingMessage.parseFrom(bytes)
    Listing(
      key = ServiceKey[Any](message.key),
      services = message.services.map(deserializeTypedActorRef).toSet,
      replyTo = deserializeTypedActorRef(message.replyTo)
    )
  }
}
