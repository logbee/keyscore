package io.logbee.keyscore.pipeline.contrib.filter

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameListParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.contrib.filter.DropRecordsLogic.fieldNamesParameter
import io.logbee.keyscore.pipeline.contrib.test.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{FreeSpec, Inside, Matchers}

@RunWith(classOf[JUnitRunner])
class DropRecordsLogicSpec extends FreeSpec with Matchers with ScalaFutures with Inside with TestSystemWithMaterializerAndExecutionContext  {

  "A DropRecordsFilterLogic" - {

    val fieldNames = FieldNameListParameter(fieldNamesParameter.ref, List("fubar"))
    val initialConfig = Configuration(parameterSet = ParameterSet(Seq(fieldNames)))

    val sample = Dataset(
      Record(
        Field("message", TextValue("Keep me!"))
      ),
      Record(
        Field("fubar", TextValue("Drop me!"))
      )
    )

    "should only let records pass which do not contain one of the specified fields" in new TestStreamForFilter[DropRecordsLogic](initialConfig) {

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
