package io.logbee.keyscore.simplepersistence.app

import akka.actor.ActorSystem
import io.logbee.keyscore.simplepersistence.JsonRestCaller
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object PersistenceApplication extends App {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = actorSystem.getDispatcher

  val logger = LoggerFactory.getLogger("PersistenceApplication")

  val caller = JsonRestCaller(
    endpoint = "http://localhost:3000/"
  )

  caller.get("").onComplete{
    case Success(value) => logger.info(value.toString())
    case Failure(exception) => logger.error(exception.toString)
    case _ => logger.error("Request did not work but don't know why")
  }

}