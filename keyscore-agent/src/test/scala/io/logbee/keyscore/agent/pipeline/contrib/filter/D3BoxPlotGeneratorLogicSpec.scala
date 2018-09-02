package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.io.{File, PrintWriter}
import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData
import io.logbee.keyscore.agent.pipeline.contrib.filter.D3BoxPlotGeneratorLogic.{groupIdentifierParameter, itemIdentifierParameter}
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, LogicParameters, StageContext}
import io.logbee.keyscore.commons.test.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.configuration.{Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class D3BoxPlotGeneratorLogicSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {
    val groupIdentifier = "id"
    val itemIdentifier = "count"
    val groupType = ""
    val itemType = ""

    val context = StageContext(system, executionContext)
    val configuration = Configuration(
      TextParameter(groupIdentifierParameter.ref, groupIdentifier),
      TextParameter(itemIdentifierParameter.ref, itemIdentifier)
    )

    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: Configuration, s: FlowShape[Dataset, Dataset]) =>
      new D3BoxPlotGeneratorLogic(LogicParameters(randomUUID(), ctx, c), s))

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

    val resultDataset = Dataset(
      Record(Field("d3_boxplot",TextValue(ExampleData.boxplot_html))
    ))
  }

  "A D3 BoxPlot" should {
    "generate the correct html data" in new TestStream {
      whenReady(filterFuture) { f =>
        sink.request(1)
        source.sendNext(d1)

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
