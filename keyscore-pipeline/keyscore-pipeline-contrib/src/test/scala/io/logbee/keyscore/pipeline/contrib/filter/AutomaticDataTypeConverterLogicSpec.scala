package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AutomaticDataTypeConverterLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A AutomaticDataTypeConverterLogic" - {

    val sample = Dataset(
      Record(
        Field("message", TextValue("hello world")),
        Field("temperature", TextValue("11.5")),
        Field("id", TextValue("42"))
      )
    )

    "should convert the fields to their appropriate data-type" in new TestStreamForFilter[AutomaticDataTypeConverterLogic](){

      whenReady(filterFuture) { _ =>

        sink.request(1)
        source.sendNext(sample)

        val result = sink.requestNext()
        result shouldBe Dataset(
          Record(
            Field("message", TextValue("hello world")),
            Field("temperature", DecimalValue(11.5)),
            Field("id", NumberValue(42))
          )
        )
      }
    }
  }
}
