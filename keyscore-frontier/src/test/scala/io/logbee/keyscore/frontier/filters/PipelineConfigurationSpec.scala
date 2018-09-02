package io.logbee.keyscore.frontier.filters

import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class PipelineConfigurationSpec extends WordSpecLike with Matchers {

  private implicit val formats = KeyscoreFormats.formats

  "PipelineConfiguration" should {
    "be deserializable" in {

      // TODO: Implement a spec to test PipelineBlueprint serialization
//      val pipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipeline.example.json"))
//      val pipelineReader2 = new InputStreamReader(getClass.getResourceAsStream("/sink.example.json"))
//      val pipelineConfiguration = read[PipelineConfiguration](pipelineReader)
//      val pipelineConfiguration2 = read[PipelineConfiguration](pipelineReader2)
//      pipelineConfiguration shouldBe a[PipelineConfiguration]
//      pipelineConfiguration shouldBe a[PipelineConfiguration]
    }
  }

}
