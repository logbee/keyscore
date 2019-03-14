package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.JsonData
import io.logbee.keyscore.JsonData._
import io.logbee.keyscore.model.blueprint.{BlueprintRef, PipelineBlueprint, SealedBlueprint, SourceBlueprint}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, MediaType}

@ExtendWith(value = Array(classOf[CitrusExtension]))
class BlueprintApiSpec extends Matchers {

  private implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[BlueprintApiSpec])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()


  @Test
  @CitrusTest
  def checkPipelineBlueprint(implicit @CitrusResource runner: TestRunner): Unit = {

    val pipelineBlueprint = loadJson(BLUEPRINTS, K2K, "pipelineBlueprint")
    val pipelineObject = loadPipelineBlueprint(K2K, "pipelineBlueprint")

    putSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)
    getSinglePipelineBlueprint(pipelineObject)
    postSinglePipelineBlueprint(pipelineObject, pipelineBlueprint)

    getAllPipelineBlueprints(1)
    deleteSinglePipelineBlueprint(pipelineObject)
    getAllPipelineBlueprints(0)

  }

  @Test
  @CitrusTest
  def checkBlueprint(implicit @CitrusResource runner: TestRunner): Unit = {

    val sourceBlueprint = loadJson(BLUEPRINTS, K2K, "sourceBlueprint")
    val sourceObject = loadSourceBlueprint(K2K, "sourceBlueprint")

    putSingleBlueprint(sourceObject, sourceBlueprint)
    getSingleBlueprint(sourceObject)
    postSingleBlueprint(sourceObject, sourceBlueprint)

    getAllBlueprints(1)
    deleteSingleBlueprint(sourceObject)
    getAllBlueprints(0)
  }

  def putSinglePipelineBlueprint(pipelineObject: PipelineBlueprint, pipelineConfig: String)(implicit runner: TestRunner): TestAction = {
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

  def postSinglePipelineBlueprint(pipelineObject: PipelineBlueprint, pipelineConfig: String)(implicit runner: TestRunner): TestAction = {
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

  def getSinglePipelineBlueprint(pipelineObject: PipelineBlueprint)(implicit runner: TestRunner): TestAction = {
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

  def getAllPipelineBlueprints(expected: Int)(implicit runner: TestRunner): TestAction = {
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

  def deleteSinglePipelineBlueprint(pipelineObject: PipelineBlueprint)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/blueprint/pipeline/${pipelineObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def putSingleBlueprint(sourceObject: SourceBlueprint, pipelineConfig: String)(implicit runner: TestRunner): TestAction = {
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

  def postSingleBlueprint(sourceObject: SourceBlueprint, pipelineConfig: String)(implicit runner: TestRunner): TestAction = {
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

  def getSingleBlueprint(sourceObject: SourceBlueprint)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val blueprints = read[SourceBlueprint](payload)
        blueprints.ref.uuid should equal(sourceObject.ref.uuid)
        log.info("GetSinglePipelineBlueprint successfully: " + blueprints.ref.uuid)
      })
    )
  }

  def getAllBlueprints(expected: Int)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/blueprint/*")
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val blueprints = read[Map[BlueprintRef, SealedBlueprint]](payload)
        blueprints should have size expected
        if (blueprints.nonEmpty) {
          log.info("GetAllPipelineBlueprints successfully: " + blueprints.head._1.uuid)
        }
      })
    )
  }

  def deleteSingleBlueprint(sourceObject: SourceBlueprint)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/blueprint/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }
}
