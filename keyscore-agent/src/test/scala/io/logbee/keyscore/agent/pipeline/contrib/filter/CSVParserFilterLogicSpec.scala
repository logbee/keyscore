package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.csvDatasetA
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, LogicParameters, StageContext}
import io.logbee.keyscore.model.configuration.{Configuration, TextListParameter, TextParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class CSVParserFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  val csv1 = Configuration(
    parameters = Seq(
      TextParameter(CSVParserFilterLogic.separatorParameter.ref, ";"),
      TextListParameter(CSVParserFilterLogic.headerParameter.ref, Seq(
        "Philosophy", "Maths", "Latin", "Astrophysics"
      ))
    )
  )

  val csv2 = Configuration(
    parameters = Seq(
      TextParameter(CSVParserFilterLogic.separatorParameter.ref, ";"),
      TextListParameter(CSVParserFilterLogic.headerParameter.ref, Seq(
        "Philosophy2", "Maths2", "Latin2", "Astrophysics2"
      ))
    )
  )

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
    val provider = (ctx: StageContext, c: Configuration, s: FlowShape[Dataset,Dataset]) => new CSVParserFilterLogic(LogicParameters(UUID.randomUUID(), ctx, c), s)
    val filterStage = new FilterStage(context, csv1, provider)

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

        Await.ready(filter.configure(csv2), 10 seconds)

        source.sendNext(csvDatasetA)
        sink.request(1)
        sink.expectNext(csvBResult)

      }
    }
  }

}

