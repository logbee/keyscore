package io.logbee.keyscore.agent

import akka.actor.{Actor, ActorLogging, Props}
import io.logbee.keyscore.agent.TestActor.Tick
import org.osgi.framework.BundleContext

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object TestActor {
  case object Tick

  def props(bundleContext: BundleContext) = Props(new TestActor(bundleContext))
}

class TestActor(bundleContext: BundleContext) extends Actor with ActorLogging {

  private implicit val ec: ExecutionContext = context.system.dispatcher

  override def preStart(): Unit = {
    context.system.scheduler.schedule(1 second, 1 second, self, Tick)
  }

  override def receive: Receive = {
    case Tick => log.info("Tick")
  }
}
