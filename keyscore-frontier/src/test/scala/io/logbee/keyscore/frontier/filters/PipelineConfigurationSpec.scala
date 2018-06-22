package io.logbee.keyscore.frontier.filters

import java.io.InputStreamReader

import io.logbee.keyscore.commons.json.helper.FilterConfigTypeHints
import io.logbee.keyscore.model.PipelineConfiguration
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class PipelineConfigurationSpec extends WordSpecLike with Matchers {

  private implicit val formats = Serialization.formats(FilterConfigTypeHints).withTypeHintFieldName("parameterType") ++ JavaTypesSerializers.all

  "PipelineConfiguration" should {
    "be deserializable" in {

      val pipelineReader = new InputStreamReader(getClass.getResourceAsStream("/pipeline.example.json"))
      val pipelineReader2 = new InputStreamReader(getClass.getResourceAsStream("/sink.example.json"))
      val pipelineConfiguration = read[PipelineConfiguration](pipelineReader)
      val pipelineConfiguration2 = read[PipelineConfiguration](pipelineReader2)
      pipelineConfiguration shouldBe a[PipelineConfiguration]
      pipelineConfiguration shouldBe a[PipelineConfiguration]
    }
  }

}
