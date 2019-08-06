package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameListParameter, ParameterSet}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class RetainFieldsLogicSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A RetainFieldsFilter" - {

    val configuration = Configuration(
      parameterSet = ParameterSet(Seq(
        FieldNameListParameter(RetainFieldsLogic.fieldNamesParameter.ref, Seq("message", "temperature"))
      ))
    )

    val sample = Dataset(Record(
      Field("message", TextValue("Hello World!")),
      Field("temperature", DecimalValue(11.5)),
      Field("fubar", TextValue("Delete Me!"))
    ))

    val modifiedSample = Dataset(Record(
      Field("message", TextValue("Hello World!")),
      Field("temperature", DecimalValue(11.5))
    ))

    "should return a Descriptor" in {
      RetainFieldsLogic.describe should not be null
    }

    "should retain only the specified fields and remove all others" in new TestStreamForFilter[RetainFieldsLogic](configuration) {

      whenReady(filterFuture) { filter =>

        source.sendNext(sample)
        sink.request(1)
        sink.expectNext(modifiedSample)
      }
    }
  }
}
