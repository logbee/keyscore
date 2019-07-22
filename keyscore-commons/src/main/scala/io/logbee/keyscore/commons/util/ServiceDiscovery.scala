package io.logbee.keyscore.commons.util

import java.util.UUID
import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import io.logbee.keyscore.commons.cluster.Topics.WhoIsTopic
import io.logbee.keyscore.commons.util.ServiceDiscovery.{Discover, Timeout}
import io.logbee.keyscore.commons.{HereIam, Service, WhoIs}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future, Promise, TimeoutException}
import scala.language.postfixOps

object ServiceDiscovery {

  def discover(services: Seq[Service], strict: Boolean = true, retries: Int = 3)(implicit context: ActorContext, timeout: FiniteDuration = 10 seconds): Future[Map[Service, ActorRef]] = {
    val promise = Promise[Map[Service, ActorRef]]
    context.system.actorOf(Props(new ServiceDiscovery(services, strict, retries, promise)), s"service-discovery-${randomUUID()}")
    promise.future
  }

  private case object Discover
  private case object Timeout
}

class ServiceDiscovery(services: Seq[Service], strict: Boolean = true, retries: Int = 3, promise: Promise[Map[Service, ActorRef]])(implicit timeout: FiniteDuration = 10 seconds) extends Actor with ActorLogging {

  import context.become

  private val mediator = DistributedPubSub(context.system).mediator
  private implicit val ec: ExecutionContextExecutor = context.dispatcher

  override def preStart(): Unit = {
    become(discovering(Map.empty, retries))
    self ! Discover
  }

  override def receive: Receive = {
    case _ =>
  }

  private def discovering(mapping: Map[Service, ActorRef], remainingRetries: Int): Receive = {

    case Discover =>
      log.debug("Started service discovery (strict={}, retries={}) for: {}", strict, remainingRetries, services.mkString(", "))
      services.foreach(service => {
        mediator ! Publish(WhoIsTopic, WhoIs(service))
      })
      context.system.scheduler.scheduleOnce(timeout, self, Timeout)
      context.become(discovering(mapping, remainingRetries - 1))

    case HereIam(service, ref) =>
      val newMapping = mapping + (service -> ref)
      log.debug(s"Discovered: $service -> $ref (${newMapping.size}/${services.size})")
      if (newMapping.size < services.size) {
        become(discovering(newMapping, remainingRetries), discardOld = true)
      }
      else {
        log.debug("Successfully finished service discovery (strict={}, retries={}) for: {}", strict, remainingRetries, services.mkString(", "))
        promise.success(newMapping)
        context.stop(self)
      }

    case Timeout if remainingRetries > 0 =>
      self ! Discover

    case Timeout if strict =>
      promise.failure(new TimeoutException(s"Service discovery did not complete within $timeout. " +
        s"The following services could not be discovered: ${services.diff(mapping.values.toSeq).mkString(",")}"))
      context.stop(self)

    case Timeout =>
      log.debug("Stopped service discovery (strict={}, retries={}) for: {}", strict, remainingRetries, services.mkString(", "))
      promise.success(mapping)
      context.stop(self)
  }
}
