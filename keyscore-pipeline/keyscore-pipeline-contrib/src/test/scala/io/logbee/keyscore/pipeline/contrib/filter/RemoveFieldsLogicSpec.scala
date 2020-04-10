package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, TextListParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.contrib.filter.RemoveFieldsLogic.fieldsToRemoveParameter
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class RemoveFieldsLogicSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A RemoveFieldsFilter" - {

    val config1 = Configuration(
      TextListParameter(fieldsToRemoveParameter, Seq("current", "timestamp"))
    )

    val config2 = Configuration(
      TextListParameter(fieldsToRemoveParameter, List("temperature", "voltage"))
    )

    val sample = Dataset(
      Record(
        Field("message", TextValue("Hello World!")),
        Field("temperature", NumberValue(42))
      ),
      Record(
        Field("message", TextValue("Have a nice day!")),
        Field("voltage", DecimalValue(7.3))
      )
    )

    val expectedUnchanged = sample

    val expected = Dataset(
      Record(
        Field("message", TextValue("Hello World!")),
      ),
      Record(
        Field("message", TextValue("Have a nice day!")),
      )
    )

    "should return a Descriptor" in {
      RemoveFieldsLogic.describe should not be null
    }

    "should remove specified fields" in new TestStreamForFilter[RemoveFieldsLogic](config1) {
      whenReady(filterFuture) { filter =>
        source.sendNext(sample)
        sink.request(1)
        sink.expectNext(expectedUnchanged)

        Await.ready(filter.configure(config2),10 seconds)

        source.sendNext(sample)
        sink.request(1)
        sink.expectNext(expected)
      }
    }
  }
}
