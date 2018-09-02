package io.logbee.keyscore.frontier.cluster

import akka.actor.{Actor, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import io.logbee.keyscore.commons.cluster.Topics.WhoIsTopic
import io.logbee.keyscore.commons.{BlueprintService, HereIam, WhoIs}

import scala.language.postfixOps

object PipelineBlueprintManager {

  def apply(): Props = Props(new PipelineBlueprintManager())
}

class PipelineBlueprintManager() extends Actor {

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(WhoIsTopic, self)
  }

  override def receive: Receive = {
    case WhoIs(BlueprintService) =>
      sender ! HereIam(BlueprintService, self)

    case _ =>

  }
}
