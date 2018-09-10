package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.JsonData
import io.logbee.keyscore.JsonData._
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.native.Serialization.read
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import org.springframework.http.{HttpStatus, MediaType}

@ExtendWith(value = Array(classOf[CitrusExtension]))
class DescriptorApiSpec extends Matchers {
  implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[DescriptorApiSpec])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()
  @Test
  @CitrusTest
  def checkDescriptor(@CitrusResource runner: TestRunner): Unit = {
    val descriptorDescriptor = loadJson(JsonData.KafkaSinkDescriptorPath)
    val sinkObject = loadExampleSinkDescriptor

    putSingleDescriptor(runner, sinkObject, descriptorDescriptor)
    getSingleDescriptor(runner, sinkObject)
    postSingleDescriptor(runner, descriptorDescriptor, sinkObject)

    getAllDescriptors(runner, 1)
    deleteSingleDescriptor(runner, sinkObject)
    getAllDescriptors(runner, 0)

    putSingleDescriptor(runner, sinkObject, descriptorDescriptor)
    deleteAllDescriptors(runner)
    getAllDescriptors(runner, 0)
  }

  def putSingleDescriptor(runner: TestRunner, sinkObj: Descriptor, sinkDesc: String): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/descriptor/${sinkObj.ref.uuid}")
      .contentType("application/json")
      .payload(sinkDesc)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def getSingleDescriptor(runner: TestRunner, sinkObj: Descriptor): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/descriptor/${sinkObj.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val descriptor = read[Descriptor](payload)
        descriptor.ref.uuid should equal(sinkObj.ref.uuid)
        descriptor should equal(sinkObj)
        descriptor.describes shouldBe sinkObj.describes
        descriptor.localization shouldBe sinkObj.localization
        log.info("GetSingleDescriptor successfully: " + descriptor.ref.uuid)
      })
    )
  }

  def deleteSingleDescriptor(runner: TestRunner, sinkObj: Descriptor): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/descriptor/${sinkObj.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def postSingleDescriptor(runner: TestRunner, sinkDescString: String, sinkObj: Descriptor): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"resources/descriptor/${sinkObj.ref.uuid}")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .messageType(MessageType.PLAINTEXT)
      .payload(sinkDescString)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }


  def getAllDescriptors(runner: TestRunner, expected: Int): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/descriptor/*")
    )
    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val descriptors = read[Map[DescriptorRef, Descriptor]](payload)
        descriptors should have size expected
        if (descriptors.nonEmpty) {
          log.info("GetAllDescriptors successfully: " + descriptors.head._1.uuid)
        }
      })
    )
  }

  def deleteAllDescriptors(runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/descriptor/*"))

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }
}
