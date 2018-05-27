package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
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
class GrokFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(context, configurationA, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new GrokFilterLogic(ctx, c, s))

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val configurationA = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List.empty)
  val configurationB = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(
    TextListParameter("fieldNames", List("message")),
    TextParameter("pattern", ".*:\\s(?<temperature>[-+]?\\d+((\\.\\d*)?|\\.\\d+)).*")
  ))

  val modified1 = Dataset(Record(record1.id, messageTextField1, NumberField("temperature", -11.5)))
  val modified2 = Dataset(Record(record2.id, messageTextField2, NumberField("temperature", 5.8)))

  "A GrokFilter" should {

    "return a MetaFilterDescriptor" in {
      GrokFilterLogic.describe should not be null
    }

    "extract data into a new field when the grok rule matches the specified field" in new TestStream {
      whenReady(filterFuture) { filter =>

        source.sendNext(dataset1)

        sink.request(1)
        sink.expectNext(dataset1)

        Await.result(filter.configure(configurationB), 10 seconds)

        source.sendNext(dataset1)
        source.sendNext(dataset2)

        sink.request(2)
        sink.expectNext(modified1)
        sink.expectNext(modified2)
      }
    }
  }
}
