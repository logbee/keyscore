package io.logbee.keyscore.simplepersistence.app

import akka.actor.ActorSystem
import io.logbee.keyscore.simplepersistence.{ConfigurationManager, JsonRestCaller}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

object PersistenceApplication extends App {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = actorSystem.getDispatcher
  implicit val materializer : ActorMaterializer = ActorMaterializer()

  val logger = LoggerFactory.getLogger("PersistenceApplication")

  val caller = JsonRestCaller(
    endpoint = "http://localhost:4711"
  )

  val configurationConfig = ConfigurationManager()
  val blueprintConfig = ConfigurationManager()
  val pipelineBlueprintConfig = ConfigurationManager()

  val configurationRequest = "/resources/configuration/*"
  val blueprintRequest = "/resources/blueprint/*"
  val pipelineBlueprintRequest = "/resources/blueprint/pipeline/*"

  while (true) {
    checkRestApiForEmptyConfigsAndRestore(configurationRequest, configurationConfig)
    checkRestApiForEmptyConfigsAndRestore(blueprintRequest, blueprintConfig)
    checkRestApiForEmptyConfigsAndRestore(pipelineBlueprintRequest,pipelineBlueprintConfig)
    Thread.sleep(1000)
  }


  private def checkRestApiForEmptyConfigsAndRestore(ApiRequest: String, configMng : ConfigurationManager) = {
    caller.get(ApiRequest).onComplete {
      case Success(value) => {
        val responseAsString: Future[String] = Unmarshal(value.entity).to[String]
        responseAsString.onComplete {
          case Success(value) => logger.info("Current available configuration for " + ApiRequest + " is " + value)
            val possibleUpdate = configMng.configNeedsUpdate(value)
            if (!possibleUpdate.isEmpty) {
              logger.info("Update the configuration for " + ApiRequest + " with " + possibleUpdate)
              //caller.post(ApiRequest, possibleUpdate)
            }
          case _ => logger.error("Can't convert string")
        }
      }
      case Failure(exception) => logger.error(exception.toString)
      case _ => logger.error("Request did not work but don't know why")
    }
  }
}