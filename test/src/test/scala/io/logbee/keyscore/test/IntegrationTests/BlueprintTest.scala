package io.logbee.keyscore.test.IntegrationTests

import com.consol.citrus.annotations.CitrusTest
import com.consol.citrus.dsl.endpoint.CitrusEndpoints
import com.consol.citrus.http.client.HttpClient
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.junit.jupiter.api.Test
import org.scalatest.Matchers
import org.slf4j.LoggerFactory

//TOOD Implement the tests and the routs @mlandth
class BlueprintTest extends Matchers {
  implicit val formats = KeyscoreFormats.formats
  private val log = LoggerFactory.getLogger(classOf[ConfiguratonTest])

  private val frontierClient: HttpClient = CitrusEndpoints.http()
    .client()
    .requestUrl("http://localhost:4711")
    .build()

  @Test
  @CitrusTest
  def checkBlueprint = ???

  //Single Blueprint

  def putSinglePipelineBlueprint = ???

  def getSinglePipelineBlueprint = ???

  def deleteSinglePipelineBlueprint = ???

  def postSinglePipelineBlueprint = ???

  def putSingleBlueprint = ???

  def getSingleBlueprint = ???

  def deleteSingleBlueprint = ???

  def postSingleBlueprint = ???

  //Multiple Blueprints

  def getAllPipelineBlueprints = ???

  def getAllBlueprints = ???
}
