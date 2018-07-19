package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.{datasetMulti1, datasetMulti2}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextListParameter}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RetainMessagesFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext  {

  trait TestStream {


    val context = StageContext(system, executionContext)
    val provider = (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new RetainMessageFilterLogic(ctx, c, s)

    val messagesToRetain = TextListParameter("messagesToRetain", List("non.+", "bartolemaeus"))
    val initialConfig = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "retainMessageFilterLogic"), List(messagesToRetain))
    val filterStage = new FilterStage(context,initialConfig,provider)

    val ((source,filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A RetainMessagesFilter" should{
    "return a MetaFilterDescriptor" in {
      RetainMessageFilterLogic.describe should not be null
    }

    "retain only the specified messages and block all others" in new TestStream {
      whenReady(filterFuture){ filter =>
        source.sendNext(datasetMulti1)
        sink.request(1)
        sink.expectNext(datasetMulti1)

        source.sendNext(datasetMulti2)
        sink.request(1)
        sink.expectNoMessage(5 seconds)
      }
    }
  }
}
