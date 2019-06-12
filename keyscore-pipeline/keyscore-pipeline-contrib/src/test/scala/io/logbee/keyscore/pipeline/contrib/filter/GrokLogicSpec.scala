package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, ParameterSet, TextListParameter, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.contrib.filter.GrokLogic.{fieldNamesParameter, patternParameter}
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class GrokLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A GrokFilter" - {

    "should return a MetaFilterDescriptor" in new TestStreamForFilter[GrokLogic]() {
      GrokLogic.describe should not be null
    }

    "when configured with no field" - {

      "should passThrough Datasets unmodified" in new TestStreamForFilter[GrokLogic]() {

        source.sendNext(dataset1)

          sink.request(1)
          sink.expectNext(dataset1)
      }
    }

    "when configured with field and pattern for one group" - {

      val configurationB = Configuration(parameterSet = ParameterSet(Seq(
        TextListParameter(fieldNamesParameter, Seq("message")),
        TextParameter(patternParameter, ".*:\\s(?<temperature>[-+]?\\d+((\\.\\d*)?|\\.\\d+)).*")
      )))

      val modified1 = Dataset(Record(messageTextField1, DecimalField("temperature", -11.5)))
      val modified2 = Dataset(Record(messageTextField2, DecimalField("temperature", 5.8)))

      "should extract data into a new field when the grok rule matches the specified field" in new TestStreamForFilter[GrokLogic](configurationB) {

          source.sendNext(dataset1)
          source.sendNext(dataset2)

          sink.requestNext().records should contain theSameElementsAs modified1.records
          sink.requestNext().records should contain theSameElementsAs modified2.records
      }
    }

    "when configured with field and pattern for multiple groups" - {

      val configurationC = Configuration(parameterSet = ParameterSet(Seq(
        TextListParameter(fieldNamesParameter, Seq("message")),
        TextParameter(patternParameter, "batman(?<earth>\\d*)_lanternId(?<corpsId>\\d*)_Justice(?<justice>[a-zA-Z]*)")
      )))

      val dataset = Dataset(Record(
        Field("message", TextValue("superman_batman52_lanternId2184_JusticeLeague_42_37.csv"))
      ))

      val expected = Dataset(Record(
        Field("message", TextValue("superman_batman52_lanternId2184_JusticeLeague_42_37.csv")),
        Field("earth", DecimalValue(52)),
        Field("corpsId", DecimalValue(2184)),
        Field("justice", TextValue("League"))
      ))

      "should extract data into multiple fields when grok rule matches the specified field" in new TestStreamForFilter[GrokLogic](configurationC) {

        source.sendNext(dataset)

          val result = sink.requestNext()
          result shouldBe expected
      }
    }
  }
}
