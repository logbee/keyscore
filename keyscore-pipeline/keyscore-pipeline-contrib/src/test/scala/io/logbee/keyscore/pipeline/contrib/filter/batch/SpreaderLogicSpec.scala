package io.logbee.keyscore.pipeline.contrib.filter.batch

import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SpreaderLogicSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val spreadedFields = List[Field](
    TextField("kind", "whether-report"),
    TextField("date", "27.03.")
  )

  val sample = Dataset(
    Record(TextField("message", "The whether is cloudy.")),
    Record(spreadedFields.head),
    Record(DecimalField("temperature", 11.5)),
    Record(spreadedFields)
  )

  "A SpreaderLogic" - {

    "when configured without any field" - {

      "should let datasets pass unmodifed" in new TestStreamForFilter[SpreaderLogic]() {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          sink.requestNext() shouldBe sample
        }
      }
    }

    "when configured with some field-names" - {

      val configuration = Configuration(parameterSet = ParameterSet(Seq(
        FieldNameListParameter(SpreaderLogic.fieldNamesParameter.ref, Seq("kind", "date"))
      )))

      "should spread the configured fields over all Records within a Dataset" in new TestStreamForFilter[SpreaderLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          sink.requestNext().records.foreach( record => {
            record.fields should contain allElementsOf spreadedFields
          })
        }
      }
    }

    "when configured with patterns" - {

      val configuration = Configuration(parameterSet = ParameterSet(Seq(
        FieldNameListParameter(SpreaderLogic.fieldNamesParameter.ref, Seq("k.*", "..te"))
      )))

      "should spread matching fields over all Records within a Dataset" in new TestStreamForFilter[SpreaderLogic](configuration) {

        whenReady(filterFuture) { _ =>

          sink.request(1)
          source.sendNext(sample)

          sink.requestNext().records.foreach( record => {
            record.fields should contain allElementsOf spreadedFields
          })
        }
      }
    }
  }
}
