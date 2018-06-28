package io.logbee.keyscore.frontier.cluster

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import io.logbee.keyscore.commons.pipeline.PipelineConfigurationResponse
import io.logbee.keyscore.frontier.cluster.PipelineConfigurationManager._
import io.logbee.keyscore.frontier.cluster.PipelineManager.RequestExistingConfigurations
import io.logbee.keyscore.model.PipelineConfiguration

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object PipelineConfigurationManager {
  def apply(pipelineManager: ActorRef) = Props(new PipelineConfigurationManager(pipelineManager))

  case class RequestConfigById(id:UUID)
  case object RequestConfigurations

  case class PipelineConfigurationById(config:PipelineConfiguration)
  case object PipelineConfigurationNotFound
  case class PipelineConfigurations(configs: List[PipelineConfiguration])
}

class PipelineConfigurationManager(pipelineManager: ActorRef) extends Actor {

  import context.{dispatcher, system}
  implicit val timeout: Timeout = 30.seconds


  private var configurations = List.empty[PipelineConfiguration]

  override def preStart(): Unit = {
    system.scheduler.schedule(1 seconds, 10 seconds) {
      (pipelineManager ? RequestExistingConfigurations()).mapTo[PipelineConfigurationResponse].onComplete{
        case Success(result) => configurations = result.pipelineConfigurations
        case Failure(_) => configurations = List.empty
      }

    }
  }

  override def receive: Receive = {

    case RequestConfigById(id) =>
      configurations.find(conf => conf.id == id) match {
        case Some(config) => sender ! PipelineConfigurationById(config)
        case None => sender ! PipelineConfigurationNotFound
      }
    case RequestConfigurations =>
      sender ! PipelineConfigurations(configurations)
  }
}
