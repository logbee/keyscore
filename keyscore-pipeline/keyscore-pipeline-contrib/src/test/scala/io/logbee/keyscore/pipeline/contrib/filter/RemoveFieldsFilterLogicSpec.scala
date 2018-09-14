package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID
import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, TextListParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.filter.RemoveFieldsFilterLogic.fieldsToRemoveParameter
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class RemoveFieldsFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)

    val config1 = Configuration(
      TextListParameter(fieldsToRemoveParameter, Seq("current", "timestamp"))
    )

    val config2 = Configuration(
      TextListParameter(fieldsToRemoveParameter, List("temperature", "voltage"))
    )

    val provider = (parameters: LogicParameters, s: FlowShape[Dataset, Dataset]) => new RemoveFieldsFilterLogic(LogicParameters(randomUUID(), context, config1), s)

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

    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, config1), provider)

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A RemoveFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      RemoveFieldsFilterLogic.describe should not be null
    }

    "remove specified fields" in new TestStream {
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