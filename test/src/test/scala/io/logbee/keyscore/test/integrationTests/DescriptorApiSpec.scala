package io.logbee.keyscore.test.integrationTests

import com.consol.citrus.TestAction
import com.consol.citrus.annotations.{CitrusResource, CitrusTest}
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension
import com.consol.citrus.dsl.runner.TestRunner
import com.consol.citrus.http.client.HttpClient
import com.consol.citrus.message.MessageType
import io.logbee.keyscore.test.util.JsonData._
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

  private implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[DescriptorApiSpec])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def checkDescriptor(implicit @CitrusResource runner: TestRunner): Unit = {

    val descriptorDescriptor = loadJson(DESCRIPTORS, K2K, "KafkaSinkLogic")
    val sinkObject = loadDescriptor(K2K, "KafkaSinkLogic")

    putSingleDescriptor(sinkObject, descriptorDescriptor)
    getSingleDescriptor(sinkObject)
    postSingleDescriptor(descriptorDescriptor, sinkObject)

    getAllDescriptors(1)
    deleteSingleDescriptor(sinkObject)
    getAllDescriptors(0)

    putSingleDescriptor(sinkObject, descriptorDescriptor)
    deleteAllDescriptors()
    getAllDescriptors(0)
  }

  def putSingleDescriptor(sinkObj: Descriptor, sinkDesc: String)(implicit runner: TestRunner): TestAction = {
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

  def getSingleDescriptor(sinkObj: Descriptor)(implicit runner: TestRunner): TestAction = {
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

  def deleteSingleDescriptor(sinkObj: Descriptor)(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/descriptor/${sinkObj.ref.uuid}")
    )

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }

  def postSingleDescriptor(sinkDescString: String, sinkObj: Descriptor)(implicit runner: TestRunner): TestAction = {
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


  def getAllDescriptors(expected: Int)(implicit runner: TestRunner): TestAction = {
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

  def deleteAllDescriptors()(implicit runner: TestRunner): TestAction = {
    runner.http(action => action.client(frontierClient)
      .send()
      .delete(s"resources/descriptor/*"))

    runner.http(action => action.client(frontierClient)
      .receive()
      .response(HttpStatus.OK))
  }
}
