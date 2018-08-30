package io.logbee.keyscore.agent.pipeline

import akka.actor.{Actor, ActorLogging, Props}
import io.logbee.keyscore.model.blueprint.SealedBlueprint

object BlueprintMaterializer {

  def apply(blueprint: SealedBlueprint): Props = Props(new BlueprintMaterializer(blueprint))
}

class BlueprintMaterializer(blueprint: SealedBlueprint) extends Actor with ActorLogging {

  override def preStart(): Unit = {

  }

  override def receive: Receive = {


  }
}
