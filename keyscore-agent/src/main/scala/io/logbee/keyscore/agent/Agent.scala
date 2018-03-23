package io.logbee.keyscore.agent

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.Agent.Initialize
import io.logbee.keyscore.commons.extension.ExtensionLoader
import io.logbee.keyscore.commons.extension.ExtensionLoader.LoadExtensions
import io.logbee.keyscore.commons.util.StartUpWatch
import io.logbee.keyscore.commons.util.StartUpWatch.StartUpComplete

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

object Agent {
  case object Initialize
}

class Agent extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher
  private val config = ConfigFactory.load()
  private val filterManager = context.actorOf(Props[FilterManager], "filter-manager")
  private val extensionLoader = context.actorOf(Props[ExtensionLoader], "extension-loader")

  override def preStart(): Unit = {
    Cluster(context.system) registerOnMemberUp {
      context.actorOf(Props(classOf[ClusterMember]), "member")
    }
  }

  override def receive: Receive = {
    case Initialize =>
      implicit val startUpTimeout: Timeout = 5 seconds
      val currentSender = sender
      val startUpWatch = context.actorOf(StartUpWatch(filterManager))
      (startUpWatch ? StartUpComplete).onComplete {
        case Success(_) =>
          extensionLoader ! LoadExtensions(config, "keyscore.agent.extensions")
          currentSender ! Done
      }
  }
}
