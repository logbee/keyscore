package io.logbee.keyscore.commons.util

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.commons.util.StartUpWatch.{Ready, StartUpComplete}

import scala.collection.mutable.ListBuffer

object StartUpWatch {
  def apply(actors: ActorRef*): Props = Props(new StartUpWatch(actors.toList))

  case object StartUpComplete
  case object Ready
}

class StartUpWatch(actors: List[ActorRef]) extends Actor with ActorLogging {

  private val observers = ListBuffer.empty[ActorRef]
  private var completionCount = actors.size

  override def preStart(): Unit = {
    actors.foreach(_ ! Ready)
  }

  override def receive: Receive = {
    case StartUpComplete =>
      observers += sender
    case Ready =>
      if (actors.contains(sender)) {
        completionCount -= 1
        if (completionCount <= 0) {
          observers.foreach(_ ! StartUpComplete)
          context.stop(self)
        }
      }
  }
}