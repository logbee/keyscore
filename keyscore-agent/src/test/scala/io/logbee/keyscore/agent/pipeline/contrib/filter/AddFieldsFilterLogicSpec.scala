package io.logbee.keyscore.agent.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.dataset1
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic.fieldListParameter
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model.configuration.{Configuration, FieldListParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
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

    val configuration = Configuration(parameters = Seq(
      FieldListParameter(fieldListParameter.ref, Seq())
    ))

    val configuration2 = Configuration(parameters = Seq(
      FieldListParameter(fieldListParameter.ref, Seq(
        Field("message3", TextValue("testValue")),
        Field("message4", TextValue("testValue2"))
    ))))

    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: Configuration, s: FlowShape[Dataset, Dataset]) => new AddFieldsFilterLogic(ctx, c, s))

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val modified1 = Dataset(records = Record(
    Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C")),
  ))

  val modified2 = Dataset(records = Record(
    Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C")),
    Field("message3", TextValue("testValue")),
    Field("message4", TextValue("testValue2"))
  ))

  "A AddFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      AddFieldsFilterLogic.describe should not be null
    }

    "add new fields and their data to the already existing data and change it's behaviour on configuration change" in new TestStream {
      whenReady(filterFuture) { filter =>

        source.sendNext(dataset1)

        sink.requestNext().records should contain theSameElementsAs modified1.records

        Await.ready(filter.configure(configuration2), 10 seconds)

        source.sendNext(dataset1)

        sink.requestNext().records should contain theSameElementsAs modified2.records
      }
    }
  }
}
