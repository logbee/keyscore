package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, FieldNameParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, _}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}


@RunWith(classOf[JUnitRunner])
class RenameFieldFromValueLogicSpec extends FreeSpec with ScalaFutures with Matchers with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val configuration = Configuration(parameterSet = ParameterSet(Seq(
      FieldNameParameter(RenameFieldFromValueLogic.sourceFieldNameParameter.ref, "kind"),
      FieldNameParameter(RenameFieldFromValueLogic.targetFieldNameParameter.ref, "value")
    )))

    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(LogicParameters(randomUUID(), context, configuration), (p: LogicParameters, s: FlowShape[Dataset, Dataset]) => new RenameFieldFromValueLogic(p, s))

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A RenameFieldFromValueLogic" - {

    "should rename the configured field by the value carried in the specified field" in new TestStream {

      val samples = Seq(
        Dataset(
          Record(
            Field("value", DecimalValue(24.8)),
            Field("kind", TextValue("ambient-temperature"))
          ),
          Record(
            Field("value", TextValue("New temperature read.")),
            Field("kind", TextValue("message")),
            Field("location", TextValue("home"))
          )
        ),
        Dataset(
          Record(
            Field("value", DecimalValue(73.42)),
            Field("kind", TextValue("unkown-temperature"))
          )
        )
      )

      val expected = Seq(
        Dataset(
          Record(
            Field("ambient-temperature", DecimalValue(24.8)),
            Field("kind", TextValue("ambient-temperature"))
          ),
          Record(
            Field("message", TextValue("New temperature read.")),
            Field("kind", TextValue("message")),
            Field("location", TextValue("home"))
          )
        ),
        Dataset(
          Record(
            Field("unkown-temperature", DecimalValue(73.42)),
            Field("kind", TextValue("unkown-temperature"))
          )
        )
      )

      whenReady(filterFuture) { _ =>

        sink.request(2)

        samples.foreach(source.sendNext)

        var actual = sink.requestNext()

        actual.records should have size 2
        actual.records.head.fields should contain only (expected.head.records.head.fields:_*)
        actual.records.last.fields should contain only (expected.head.records.last.fields:_*)

        actual = sink.requestNext()

        actual.records should have size 1
        actual.records.head.fields should contain only (expected.last.records.head.fields:_*)
      }
    }

    "should let pass datasets which does not contain any of the expected fields." in new TestStream {

      val samples = Seq(
        Dataset(
          Record(
            Field("message", TextValue("Hello World!")),
          )
        ),
        Dataset(
          Record(
            Field("message", TextValue("Hello World!")),
            Field("temperature", DecimalValue(42))
          ),
          Record(
            Field("device", TextValue("C100")),
          )
        )
      )

      whenReady(filterFuture) { _ =>

        sink.request(2)

        samples.foreach(source.sendNext)

        sink.requestNext() shouldBe samples.head
        sink.requestNext() shouldBe samples.last
      }
    }

    "should let pass datasets which does not contain one of the expected fields." in new TestStream {

      val sampleA = Dataset(
        Record(
          Field("message", TextValue("Hello World!")),
          Field("kind", TextValue("temperature")),
        )
      )

      val sampleB = Dataset(
        Record(
          Field("value", TextValue("Hello World!")),
          Field("type", TextValue("temperature")),
        )
      )

      whenReady(filterFuture) { _ =>

        sink.request(2)

        source.sendNext(sampleA)
        source.sendNext(sampleB)

        sink.requestNext() shouldBe sampleA
        sink.requestNext() shouldBe sampleB
      }
    }
  }
}
