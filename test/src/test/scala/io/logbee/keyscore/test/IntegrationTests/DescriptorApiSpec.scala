package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.Using
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
    val descriptorConfiguration = Using.using(getClass.getResourceAsStream("/JSONFiles/descriptors/KafkaSinkLogic.json")) { stream =>
      scala.io.Source.fromInputStream(stream).mkString
    }
    val sinkObject = read[Descriptor](descriptorConfiguration)

    putSingleDescriptor(runner, sinkObject, descriptorConfiguration)
    getSingleDescriptor(runner, sinkObject)
    postSingleDescriptor(runner, descriptorConfiguration, sinkObject)

    getAllDescriptors(runner, 1)
    deleteSingleDescriptor(runner, sinkObject)
    getAllDescriptors(runner, 0)

    putSingleDescriptor(runner, sinkObject, descriptorConfiguration)
    deleteAllDescriptors(runner)
    getAllDescriptors(runner, 0)
  }

  def putSingleDescriptor(runner: TestRunner, sourceObject: Descriptor, sinkConfig: String): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .put(s"/resources/descriptor/${sourceObject.ref.uuid}")
      .contentType("application/json")
      .payload(sinkConfig)
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.CREATED)
    )
  }

  def getSingleDescriptor(runner: TestRunner, sourceObject: Descriptor): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .get(s"resources/descriptor/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK)
      .validationCallback((message, context) => {
        val payload = message.getPayload().asInstanceOf[String]
        val descriptor = read[Descriptor](payload)
        descriptor.ref.uuid should equal(sourceObject.ref.uuid)
        descriptor should equal(sourceObject)
        descriptor.describes shouldBe sourceObject.describes
        descriptor.localization shouldBe sourceObject.localization
        log.info("GetSingleDescriptor successfully: " + descriptor.ref.uuid)
      })
    )
  }

  def deleteSingleDescriptor(runner: TestRunner, sourceObject: Descriptor): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/descriptor/${sourceObject.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def postSingleDescriptor(runner: TestRunner, sourceConfigString: String, sourceObject: Descriptor): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .post(s"resources/descriptor/${sourceObject.ref.uuid}")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .messageType(MessageType.PLAINTEXT)
      .payload(sourceConfigString)
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
