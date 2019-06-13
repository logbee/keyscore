package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.contrib.filter.GrokLogic.{autoDetectParameter, fieldNamesParameter, patternParameter}
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class GrokLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A GrokFilter" - {

    val sample = Dataset(Record(Field("message", TextValue("superman_tRuE_batman52.52_lanternId2184_JusticeLeague_42_37.csv"))))

    case class Fixture(sample: Dataset, expectation: Dataset, configuration: Configuration, title: String)

    Seq(
      Fixture(
        sample,
        expectation = Dataset(Record(
          Field("message", TextValue("superman_tRuE_batman52.52_lanternId2184_JusticeLeague_42_37.csv")),
          Field("isCool", BooleanValue(true)),
          Field("earth", DecimalValue(52.52)),
          Field("corpsId", NumberValue(2184)),
          Field("justice", TextValue("League"))
        )),
        configuration = Configuration(parameterSet = ParameterSet(Seq(
          TextListParameter(fieldNamesParameter, Seq("message")),
          TextParameter(patternParameter, "superman_(?<isCool>\\w*)_batman(?<earth>.*)_lanternId(?<corpsId>\\d*)_Justice(?<justice>[a-zA-Z]*)"),
          BooleanParameter(autoDetectParameter, true)
        ))),
        title = "when configured with enabled auto-detection, should extract data into their expected type"
      ),
      Fixture(
        sample,
        expectation = Dataset(Record(
          Field("message", TextValue("superman_tRuE_batman52.52_lanternId2184_JusticeLeague_42_37.csv")),
          Field("isCool", TextValue("tRuE")),
          Field("earth", TextValue("52.52")),
          Field("corpsId", TextValue("2184")),
          Field("justice", TextValue("League"))
        )),
        configuration = Configuration(parameterSet = ParameterSet(Seq(
          TextListParameter(fieldNamesParameter, Seq("message")),
          TextParameter(patternParameter, "superman_(?<isCool>\\w*)_batman(?<earth>.*)_lanternId(?<corpsId>\\d*)_Justice(?<justice>[a-zA-Z]*)"),
          BooleanParameter(autoDetectParameter, false)
        ))),
        title = "when configured with disabled auto-detection, should extract data into TextValues"
      ),
      Fixture(
        sample = Dataset(Record(
          Field("message", TextValue("The weather is cloudy with a current temperature of 11.5"))
        )),
        expectation = Dataset(Record(
          Field("message", TextValue("The weather is cloudy with a current temperature of 11.5"))
        )),
        configuration = Configuration(),
        title = "when unconfigured, should passthrough datasets unmodified"
      )
    ).foreach{ case Fixture(sample, expectation, configuration, title) =>

      s"$title" in new TestStreamForFilter[GrokLogic](configuration) {
        source.sendNext(sample)
        sink.requestNext() shouldBe expectation
      }
    }

    "should return a Descriptor" in new TestStreamForFilter[GrokLogic]() {
      GrokLogic.describe should not be null
    }
  }
}
