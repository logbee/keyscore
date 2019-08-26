package io.logbee.keyscore.commons.util

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.typesafe.config.Config
import io.logbee.keyscore.commons.util.StartUpWatch.{CheckReadiness, Ready, StartUpComplete, StartUpFailed}
import io.logbee.keyscore.model.util.ToFiniteDuration.asFiniteDuration

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object StartUpWatch {
  def apply(actors: ActorRef*): Props = Props(new StartUpWatch(actors.toList))
  
  case object CheckReadiness
  case object StartUpComplete
  case object StartUpFailed
  case object Ready
  
  
  object Configuration {
    def apply(config: Config): Configuration = {
      val sub = config.getConfig("keyscore.startup-watch")
      new Configuration(
        retryInterval = sub.getDuration("retry-interval"),
        maxRetries = sub.getInt("max-retries"),
      )
    }
  }
  case class Configuration(retryInterval: FiniteDuration, maxRetries: Int)
}

class StartUpWatch(actors: List[ActorRef]) extends Actor with ActorLogging {
  
  private val configuration = StartUpWatch.Configuration(context.system.settings.config)
  private var retriesLeft = configuration.maxRetries
  
  private implicit val ec: ExecutionContext = context.system.dispatcher
  
  private val observers = ListBuffer.empty[ActorRef]
  private val actorsReady = mutable.HashMap.from(actors.map(_ -> false))


  override def preStart(): Unit = {
    self ! CheckReadiness
  }

  override def receive: Receive = {
    case CheckReadiness =>
      log.debug("Checking readiness of: {}", actors.mkString("[", ", ", "]"))
      actorsReady
        .filter(_._2 == false)
        .foreach(_._1 ! Ready)
      
      if (retriesLeft > 0) {
        context.system.scheduler.scheduleOnce(configuration.retryInterval, self, CheckReadiness)
        retriesLeft -= 1
      }
      else {
        observers.foreach(_ ! StartUpFailed)
      }
      
    case StartUpComplete =>
      observers += sender
      
    case Ready =>
      if (actors.contains(sender)) {
        actorsReady.put(sender, true)
        
        val allAreReady = actorsReady.forall(actorState => actorState._2)
        if (allAreReady) {
          observers.foreach(_ ! StartUpComplete)
          context.stop(self)
        }
      }
  }
}
