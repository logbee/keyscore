package io.logbee.keyscore.pipeline.contrib.filter

import java.io.{File, PrintWriter}

import io.logbee.keyscore.model.configuration.{Configuration, TextParameter}
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.pipeline.contrib.filter.D3BoxPlotGeneratorLogic.{groupIdentifierParameter, itemIdentifierParameter}
import io.logbee.keyscore.pipeline.testkit.{TestActorSystem, TestStreamForFilter}
import io.logbee.keyscore.test.fixtures.ExampleData
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class D3BoxPlotGeneratorLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestActorSystem {

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

  "A D3 BoxPlot" - {

    val groupIdentifier = "id"
    val itemIdentifier = "count"
    val groupType = ""
    val itemType = ""

    val configuration = Configuration(
      TextParameter(groupIdentifierParameter.ref, groupIdentifier),
      TextParameter(itemIdentifierParameter.ref, itemIdentifier)
    )

    "should generate the correct html data" in new TestStreamForFilter[D3BoxPlotGeneratorLogic](configuration) {

      whenReady(filterFuture) { _ =>
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
