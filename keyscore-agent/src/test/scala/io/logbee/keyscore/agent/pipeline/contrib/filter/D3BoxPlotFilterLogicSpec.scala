package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.io.{File, PrintWriter}

import io.logbee.keyscore.agent.pipeline.{ExampleData, TestSystemWithMaterializerAndExecutionContext}
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextParameter}
import org.scalatest.{FreeSpec, Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class D3BoxPlotFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {
    val groupIdentifier = "id"
    val itemIdentifier = "count"
    val groupType = ""
    val itemType = ""

    val context = StageContext(system, executionContext)
    val configuration = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(
      TextParameter("groupIdentifier", groupIdentifier),
      TextParameter("itemIdentifier", itemIdentifier),
      TextParameter("groupType", groupType),
      TextParameter("itemType", itemType)
    ))

    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) =>
      new D3BoxPlotFilterLogic(ctx, c, s))

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()

    val d1 = Dataset(
      Record(
        Field("id", NumberValue(1)),
        Field("count", NumberValue(14))
      ),
      Record(
        Field("id", NumberValue(1)),
        Field("count", NumberValue(17))
      ),
      Record(
        Field("id", NumberValue(1)),
        Field("count", NumberValue(19))
      ),
      Record(
        Field("id", NumberValue(1)),
        Field("count", NumberValue(12))
      ),
      Record(
        Field("id", NumberValue(1)),
        Field("count", NumberValue(15))
      )
    )
    val d2 = Dataset(
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(24))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(18))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(21))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(22))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(25))
      )
    )
    val d3 = Dataset(
      Record(
        Field("id", NumberValue(3)),
        Field("count", NumberValue(12))
      ),
      Record(
        Field("id", NumberValue(3)),
        Field("count", NumberValue(14))
      ),
      Record(
        Field("id", NumberValue(3)),
        Field("count", NumberValue(17))
      ),
      Record(
        Field("id", NumberValue(3)),
        Field("count", NumberValue(13))
      ),
      Record(
        Field("id", NumberValue(3)),
        Field("count", NumberValue(19))
      )
    )
    val d4 = Dataset(
      Record(
        Field("id", NumberValue(4)),
        Field("count", NumberValue(27))
      ),
      Record(
        Field("id", NumberValue(4)),
        Field("count", NumberValue(31))
      ),
      Record(
        Field("id", NumberValue(4)),
        Field("count", NumberValue(22))
      ),
      Record(
        Field("id", NumberValue(4)),
        Field("count", NumberValue(29))
      ),
      Record(
        Field("id", NumberValue(4)),
        Field("count", NumberValue(32))
      )
    )
    val d5 = Dataset(
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(24))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(11))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(24))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(25))
      ),
      Record(
        Field("id", NumberValue(2)),
        Field("count", NumberValue(27))
      )
    )

    val resultDataset = Dataset(
      Record(Field("d3_boxplot",TextValue(ExampleData.boxplot_html))
    ))

  }

  "A D3 BoxPlot" should {
    "generate the correct html data" in new TestStream {
      whenReady(filterFuture) { f =>
        sink.request(2)
        source.sendNext(d1)
        source.sendNext(d2)

        sink.requestNext()
        val resultRecords = sink.requestNext().records
        resultRecords should contain theSameElementsAs resultDataset.records

        //File is located under /path/to/your/repo/generated_boxplot.html
        val file = new File("generated_boxplot.html")
        val pw = new PrintWriter(file, "UTF-8")
        val res = resultRecords.head.fields.head.toTextField.value
        pw.write(res)
        pw.close
      }

    }
  }

}
