package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.stream.ExampleData.{csvDatasetA, csvDatasetB,csvHeader,csvHeader2,csvA}
import io.logbee.keyscore.agent.stream.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.stream.stage.{DefaultFilterStage, FilterStage, StageContext}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextMapParameter}
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
    csvA.id,
    TextField("Philosophy" , "13"),
    TextField("Maths" , "07"),
    TextField("Latin" , "09"),
    TextField("Astrophysics" , "15")
  ))

  val csvBResult = Dataset(Record(
    csvA.id,
    TextField("Philosophy2" , "13"),
    TextField("Maths2" , "07"),
    TextField("Latin2" , "09"),
    TextField("Astrophysics2" , "15")
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

