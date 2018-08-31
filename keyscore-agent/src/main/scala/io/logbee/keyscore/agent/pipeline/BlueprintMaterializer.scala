package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import io.logbee.keyscore.model.blueprint.SealedBlueprint

object BlueprintMaterializer {

  def apply(blueprint: SealedBlueprint): Props = Props(new BlueprintMaterializer(blueprint))
}

class BlueprintMaterializer(blueprint: SealedBlueprint, configurationManager: Option[ActorRef] = None) extends Actor with ActorLogging {

  private val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {


  }

  override def receive: Receive = {

  }
}
