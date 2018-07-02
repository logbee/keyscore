package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.{dataset1, record1}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class AddFieldsFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {


  trait TestStream {

    val configuration = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(TextMapParameter("fieldsToAdd", Map.empty)))
    val configuration2 = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(TextMapParameter("fieldsToAdd", Map("message3" -> "testValue", "message4" -> "testValue2"))))
    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new AddFieldsFilterLogic(ctx, c, s))

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val modified1 = Dataset(Record(
    record1.id,
    TextField("message", "The weather is cloudy with a current temperature of: -11.5 C"),
  ))

  val modified2 = Dataset(Record(
    record1.id,
    TextField("message", "The weather is cloudy with a current temperature of: -11.5 C"),
    TextField("message3", "testValue"),
    TextField("message4", "testValue2")
  ))


  "A AddFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      AddFieldsFilterLogic.describe should not be null
    }

    "add new fields and their data to the already existing data and change it's behaviour on configuration change" in new TestStream {
      whenReady(filterFuture) { filter =>

        source.sendNext(dataset1)

        sink.request(1)
        sink.expectNext(modified1)

        Await.ready(filter.configure(configuration2), 10 seconds)

        source.sendNext(dataset1)

        sink.request(1)
        sink.expectNext(modified2)
      }

    }
  }
}
