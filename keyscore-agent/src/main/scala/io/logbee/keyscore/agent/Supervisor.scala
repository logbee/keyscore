package io.logbee.keyscore.agent

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import io.logbee.keyscore.agent.Supervisor.SetupStream
import io.logbee.keyscore.model.{FilterBlueprint, StreamBlueprint}

object Supervisor {
  case class SetupStream(streamBlueprint: StreamBlueprint)
}

class Supervisor extends Actor {

  val actors = Map.empty[UUID,Map[UUID, ActorRef]]

  override def receive: Receive = {
    case SetupStream(streamBlueprint) =>

      var lastFilter: ActorRef = None[ActorRef]
      streamBlueprint.filters.reverse.foreach(filterBlueprint => {
        filterBlueprint.filterType match {
          case "kafka-input" =>
            lastFilter = context.actorOf(KafkaInputFilter.props(filterBlueprint, lastFilter))
            actors.getOrElse(streamBlueprint.id, Map.empty) += filterBlueprint.id -> lastFilter
          case "regex" =>
            lastFilter = context.actorOf(RegexFilterActor.props(filterBlueprint, lastFilter))
            actors.getOrElse(streamBlueprint.id, Map.empty) += filterBlueprint.id -> lastFilter
        }
      })
  }
}
