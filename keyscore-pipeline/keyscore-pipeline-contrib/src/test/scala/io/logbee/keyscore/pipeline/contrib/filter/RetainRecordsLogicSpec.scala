package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameListParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.contrib.test.TestStreamFor
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Inside, Matchers}

@RunWith(classOf[JUnitRunner])
class RetainRecordsLogicSpec extends FreeSpec with Matchers with ScalaFutures with MockFactory with Inside with TestSystemWithMaterializerAndExecutionContext  {



  "A RetainRecordsFilterLogic" - {

    val configuration = Configuration(
      FieldNameListParameter(RetainRecordsLogic.fieldNamesParameter, List("message", "fubar"))
    )

    val sample = Dataset(
      Record(
        Field("message", TextValue("Keep me!")),
        Field("fubar", TextValue("Keep me too!"))
      ),
      Record(
        Field("fubar", TextValue("Drop me!"))
      )
    )

    "should only let records pass which contain all of the specified fields" in new TestStreamFor[RetainRecordsLogic](configuration) {

      whenReady(filterFuture){ _ =>

        source.sendNext(sample)
        sink.request(1)

        val actual = sink.expectNext()

        actual.records should have size 1

        inside(actual.records.head.fields.head) { case Field("message", TextValue(value)) =>
          value shouldBe "Keep me!"
        }
      }
    }
  }
}
