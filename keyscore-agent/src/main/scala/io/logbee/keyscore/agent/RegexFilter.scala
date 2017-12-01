package io.logbee.keyscore.agent

import RegexFilterActor.{Event, Result}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.logbee.keyscore.model.FilterBlueprint

import scala.util.matching.Regex

object RegexFilterActor {
  def props(blueprint: FilterBlueprint, nextFilter: ActorRef) : Props =  {
    val parameters = blueprint.parameters
    Props(new RegexFilterActor(parameters("regexRule"), nextFilter))
    throw new IllegalArgumentException
  }

  case class Event(message: String)
  case class Result(event: Event, properties: Map[String,String])
}

class RegexFilterActor(regex: String, nextFilter: ActorRef) extends Actor with ActorLogging{

  private val groupNamePatter: Regex = "\\(\\?<(\\w*)>".r

  override def receive: Receive = {
    case event @ Event(message) =>
      val patter = regex.r(groupNamePatter.findAllMatchIn(regex).map(_.group(1)).toSeq: _*)
      val properties = Map(patter.findFirstMatchIn(message).map(m => m.groupNames.map( name => (name, m.group(name)))).get: _*)

      println(s"RegexFilterActor: $properties")
  }
}
