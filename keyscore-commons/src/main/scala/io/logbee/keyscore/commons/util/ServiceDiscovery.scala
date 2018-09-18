package io.logbee.keyscore.commons.util

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import io.logbee.keyscore.commons.cluster.Topics.WhoIsTopic
import io.logbee.keyscore.commons.util.ServiceDiscovery.Timeout
import io.logbee.keyscore.commons.{HereIam, Service, WhoIs}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise, TimeoutException}

object ServiceDiscovery {

  def discover(services: Seq[Service], strict: Boolean = true)(implicit context: ActorContext, timeout: FiniteDuration = 10 seconds): Future[Map[Service, ActorRef]] = {
    val promise = Promise[Map[Service, ActorRef]]
    context.actorOf(Props(new ServiceDiscovery(services, strict, promise)))
    promise.future
  }

  private case object Timeout
}

class ServiceDiscovery(services: Seq[Service], strict: Boolean = true, promise: Promise[Map[Service, ActorRef]])(implicit timeout: FiniteDuration = 10 seconds) extends Actor with ActorLogging {

  import context.become

  private val mediator = DistributedPubSub(context.system).mediator
  private implicit val ec = context.dispatcher

  override def preStart(): Unit = {

    become(discovering(Map.empty))

    services.foreach(service => {
      mediator ! Publish(WhoIsTopic, WhoIs(service))
    })

    context.system.scheduler.scheduleOnce(timeout, self, Timeout)

    log.debug(s"Started service discovery (strict=$strict) for: ${services.mkString(", ")}")
  }

  override def receive: Receive = {
    case _ =>
  }

  private def discovering(mapping: Map[Service, ActorRef]): Receive = {

    case HereIam(service, ref) =>
      val newMapping = mapping + (service -> ref)
      log.debug(s"Discoverd: $service -> $ref (${newMapping.size}/${services.size})")
      if (newMapping.size < services.size) {
        become(discovering(newMapping), discardOld = true)
      }
      else {
        log.debug(s"Successfully finished service discovery (strict=$strict) for: ${services.mkString(", ")}")
        promise.success(newMapping)
        context.stop(self)
      }

    case Timeout if strict =>
      promise.failure(new TimeoutException(s"Service discovery did not complete within $timeout. " +
        s"The following services could not be discovered: ${services.diff(mapping.values.toSeq).mkString(",")}"))
      context.stop(self)

    case Timeout =>
      log.debug(s"Stopped service discovery (strict=$strict) for: ${services.mkString(", ")}")
      promise.success(mapping)
      context.stop(self)
  }
}
