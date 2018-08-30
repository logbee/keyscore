package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.{datasetMulti1, datasetMulti2}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, LogicParameters, StageContext}
import io.logbee.keyscore.model.configuration.{Configuration, TextListParameter}
import io.logbee.keyscore.model.data.Dataset
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class DropMessageFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext  {

  trait TestStream {


    val context = StageContext(system, executionContext)
    val provider = (ctx: StageContext, c: Configuration, s: FlowShape[Dataset, Dataset]) => new DropMessageFilterLogic(LogicParameters(randomUUID(), ctx, c), s)

    val messagesToDrop = TextListParameter("messagesToRetain", List("non.+", "bartolemaeus"))
    val initialConfig = Configuration(parameters = Seq(messagesToDrop))
    val filterStage = new FilterStage(context, initialConfig, provider)

    val ((source,filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A DropMessageFilter" should{
    "return a MetaFilterDescriptor" in {
      DropMessageFilterLogic.describe should not be null
    }

    "drop only the specified messages" in new TestStream {
      whenReady(filterFuture){ filter =>
        source.sendNext(datasetMulti2)
        sink.request(1)
        sink.expectNext(datasetMulti2)

        source.sendNext(datasetMulti1)
        sink.request(1)
        sink.expectNoMessage(5 seconds)
      }
    }
  }
}
