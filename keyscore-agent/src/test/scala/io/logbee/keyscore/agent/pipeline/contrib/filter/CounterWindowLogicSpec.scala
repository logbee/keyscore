package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class CounterWindowLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  val configuration = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(
    IntParameter("amount", 3),
  ))

  val context = StageContext(system, executionContext)
  val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new CounterWindowingLogic(ctx, c, s))

  val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
    .viaMat(filterStage)(Keep.both)
    .toMat(TestSink.probe[Dataset])(Keep.both)
    .run()

  val sample1 = Dataset(Record(
    Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C"))
  ))

  val sample2 = Dataset(Record(
    Field("message", TextValue("Is is a rainy day. Temperature: 5.8 C"))
  ))

  val sample3 = Dataset(Record(
    Field("message", TextValue("The weather is sunny with a current temperature of: 14.4 C")),
    Field("location", TextValue("ulm/germany"))
  ))

  "A CounterWindowingLogic" - {

    "should return a MetaFilterDescriptor" in {
      CounterWindowingLogic.describe should not be null
    }

    "should buffer datasets until the specified amount has been reached" in {

      whenReady(filterFuture) { filter =>

        sink.request(1)
        source.sendNext(sample1)
        source.sendNext(sample2)
        sink.expectNoMessage(3 seconds)
        source.sendNext(sample3)
        sink.requestNext().records should have size 3
        source.sendNext(sample1)
        sink.expectNoMessage(3 seconds)
        source.sendNext(sample2)
        source.sendNext(sample3)
        sink.requestNext().records should have size 3
      }
    }
  }
}
