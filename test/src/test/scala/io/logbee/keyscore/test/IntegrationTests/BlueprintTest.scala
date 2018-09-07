package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.Using
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, MediaType}

import scala.io.Source.fromInputStream

//TODO Implement the tests and the routs @mlandth
@ExtendWith(value = Array(classOf[CitrusExtension]))
class BlueprintTest extends Matchers {
  implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[BlueprintTest])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def checkBlueprint(@CitrusResource runner: TestRunner): Unit = {

    val pipelineBlueprint = Using.using(getClass.getResourceAsStream("/JSONFiles/blueprints/pipelineBlueprint.json")) { stream =>
      fromInputStream(stream).mkString
    }
    val pipelineObject = read[PipelineBlueprint](pipelineBlueprint)

    val sourceBlueprint = Using.using(getClass.getResourceAsStream("/JSONFiles/blueprints/sourceBlueprint.json")) { stream =>
      fromInputStream(stream).mkString
    }
    val sourceObject = read[SourceBlueprint](pipelineBlueprint)

    //Pipeline Blueprint
    putSinglePipelineBlueprint(runner, pipelineObject, pipelineBlueprint)
    getSinglePipelineBlueprint(runner, pipelineObject)
    postSinglePipelineBlueprint(runner, pipelineObject, pipelineBlueprint)

    getAllPipelineBlueprints(runner, 1)
    deleteSinglePipelineBlueprint(runner, pipelineObject)
    getAllPipelineBlueprints(runner, 0)
//
//    //Source Blueprint
//    putSingleBlueprint(runner, sourceObject, sourceBlueprint)
//    getSingleBlueprint(runner, sourceObject)
//    postSingleBlueprint(runner, sourceObject, sourceBlueprint)
//
//    getAllBlueprints(runner, 1)
//    deleteSingleBlueprint(runner, sourceObject)
//    getAllBlueprints(runner, 0)
  }
  //Pipeline Blueprint

  def putSinglePipelineBlueprint(runner: TestRunner, pipelineObject: PipelineBlueprint, pipelineConfig: String) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
      .contentType("application/json")
      .payload(pipelineConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def postSinglePipelineBlueprint(runner: TestRunner, pipelineObject: PipelineBlueprint, pipelineConfig: String) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .messageType(MessageType.PLAINTEXT)
      .payload(pipelineConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def getSinglePipelineBlueprint(runner: TestRunner, pipelineObject: PipelineBlueprint) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val pipelineBlueprint = read[PipelineBlueprint](payload)
        pipelineBlueprint.ref.uuid should equal(pipelineObject.ref.uuid)
        log.info("GetSinglePipelineBlueprint successfully: " + pipelineBlueprint.ref.uuid)
      })
    )
  }

  def getAllPipelineBlueprints(runner: TestRunner, expected: Int) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/pipeline/*")
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val pipelineBlueprints = read[Map[BlueprintRef, PipelineBlueprint]](payload)
        pipelineBlueprints should have size expected
        if (pipelineBlueprints.nonEmpty) {
          log.info("GetAllPipelineBlueprints successfully: " + pipelineBlueprints.head._1.uuid)
        }
      })
    )
  }

  def deleteSinglePipelineBlueprint(runner: TestRunner, pipelineObject: PipelineBlueprint) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def putSingleBlueprint(runner: TestRunner, sourceObject: SourceBlueprint, pipelineConfig: String) = {
    println(sourceObject)

    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/blueprint/${sourceObject.ref.uuid}")
      .contentType("application/json")
      .payload(pipelineConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def postSingleBlueprint(runner: TestRunner, sourceObject: SourceBlueprint, pipelineConfig: String) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"resources/blueprint/${sourceObject.ref.uuid}")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .messageType(MessageType.PLAINTEXT)
      .payload(pipelineConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def getSingleBlueprint(runner: TestRunner, sourceObject: SourceBlueprint) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val blueprints = read[PipelineBlueprint](payload)
        blueprints.ref.uuid should equal(sourceObject.ref.uuid)
        log.info("GetSinglePipelineBlueprint successfully: " + blueprints.ref.uuid)
      })
    )
  }

  def getAllBlueprints(runner: TestRunner, expected: Int) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/*")
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val blueprints = read[Map[BlueprintRef, PipelineBlueprint]](payload)
        blueprints should have size expected
        if (blueprints.nonEmpty) {
          log.info("GetAllPipelineBlueprints successfully: " + blueprints.head._1.uuid)
        }
      })
    )
  }

  def deleteSingleBlueprint(runner: TestRunner, sourceObject: SourceBlueprint) = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/blueprint/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }
}
