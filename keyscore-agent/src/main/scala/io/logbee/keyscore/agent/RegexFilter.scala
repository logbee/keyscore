
import RegexFilterActor.{Event, Result}
import akka.actor.{Actor, ActorLogging, Props}

import scala.util.matching.Regex

object RegexFilterActor {
  def props(regex: String) : Props =  Props(new RegexFilterActor(regex))

  case class Event(message: String)
  case class Result(event: Event, properties: Map[String,String])
}

class RegexFilterActor(regex: String) extends Actor with ActorLogging{

  private val groupNamePatter: Regex = "\\(\\?<(\\w*)>".r

  override def receive: Receive = {
    case event @ Event(message) =>
      val patter = regex.r(groupNamePatter.findAllMatchIn(regex).map(_.group(1)).toSeq: _*)
      val properties = Map(patter.findFirstMatchIn(message).map(m => m.groupNames.map( name => (name, m.group(name)))).get: _*)

      println(s"RegexFilterActor: $properties")
  }
}
