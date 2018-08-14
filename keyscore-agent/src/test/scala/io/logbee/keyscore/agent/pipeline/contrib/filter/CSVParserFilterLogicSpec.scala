package io.logbee.keyscore.agent.pipeline.contrib.filter

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.{csvDatasetA, csvHeader, csvHeader2}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.FilterConfiguration
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class CSVParserFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  val csvAResult = Dataset(Record(
    Field("Philosophy", TextValue("13")),
    Field("Maths", TextValue("07")),
    Field("Latin", TextValue("09")),
    Field("Astrophysics", TextValue("15"))
  ))

  val csvBResult = Dataset(Record(
    Field("Philosophy2", TextValue("13")),
    Field("Maths2", TextValue("07")),
    Field("Latin2", TextValue("09")),
    Field("Astrophysics2", TextValue("15"))
  ))

  trait TestStream {

    val context = StageContext(system, executionContext)
    val provider = (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset,Dataset]) => new CSVParserFilterLogic(ctx, c, s)
    val filterStage = new FilterStage(context, csvHeader, provider)

    val ((source,filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A CSVFilterFunction" should {

    "return a MetaFilterDescriptor" in {
      CSVParserFilterLogic.describe should not be null
    }

    "convert a csv string into a normal record" in new TestStream {
      whenReady(filterFuture) { filter =>
        source.sendNext(csvDatasetA)
        sink.request(1)
        sink.expectNext(csvAResult)

        Await.ready(filter.configure(csvHeader2),10 seconds)

        source.sendNext(csvDatasetA)
        sink.request(1)
        sink.expectNext(csvBResult)

      }
    }
  }

}

