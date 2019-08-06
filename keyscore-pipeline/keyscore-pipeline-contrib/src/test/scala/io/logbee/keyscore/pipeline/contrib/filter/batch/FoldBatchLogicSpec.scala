package io.logbee.keyscore.pipeline.contrib.filter.batch

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Record, _}
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class FoldBatchLogicSpec extends FreeSpec with ScalaFutures with Matchers with TestSystemWithMaterializerAndExecutionContext {

  "A FoldBatchLogic" - {

    val sample = Dataset(
      Record(
        Field("message", TextValue("Hello World"))
      ),
      Record(
        Field("message", TextValue("Bye Bye")),
        Field("temperature", DecimalValue(13.37))
      ),
      Record(
        Field("temperature", DecimalValue(47.11))
      )
    )

    "when processing left-to-right" - {

      val configuration = Configuration(parameterSet = ParameterSet(Seq(
        FieldNameParameter(FoldBatchLogic.orderParameter, "LEFT")
      )))

      "should overwrite the first with the last" in new TestStreamForFilter[FoldBatchLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          val actual = sink.requestNext()
          actual.records should have size 1
          actual.records.head.fields should contain only (
            Field("message", TextValue("Bye Bye")),
            Field("temperature", DecimalValue(47.11))
          )
        }
      }
    }

    "when processing right-to-left" - {

      val configuration = Configuration(parameterSet = ParameterSet(Seq(
        FieldNameParameter(FoldBatchLogic.orderParameter, "RIGHT")
      )))

      "should overwrite the last with the first" in new TestStreamForFilter[FoldBatchLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          val actual = sink.requestNext()
          actual.records should have size 1
          actual.records.head.fields should contain only (
            Field("message", TextValue("Hello World")),
            Field("temperature", DecimalValue(13.37))
          )
        }
      }
    }
  }
}
