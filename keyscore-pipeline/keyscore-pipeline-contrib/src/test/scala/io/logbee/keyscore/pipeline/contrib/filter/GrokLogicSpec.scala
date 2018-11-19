package io.logbee.keyscore.pipeline.contrib.filter

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.model.configuration.{Configuration, TextListParameter, TextParameter}
import io.logbee.keyscore.model.data.{Dataset, DecimalField, Record}
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.filter.GrokLogic.{fieldNamesParameter, patternParameter}
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class GrokLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)
    val provider = (parameters: LogicParameters, s: FlowShape[Dataset,Dataset]) => new GrokLogic(parameters, s)
    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, configurationA), provider)

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val configurationA = Configuration()
  val configurationB = Configuration(parameters = Seq(
    TextListParameter(fieldNamesParameter, Seq("message")),
    TextParameter(patternParameter, ".*:\\s(?<temperature>[-+]?\\d+((\\.\\d*)?|\\.\\d+)).*")
  ))

  val modified1 = Dataset(Record(messageTextField1, DecimalField("temperature", -11.5)))
  val modified2 = Dataset(Record(messageTextField2, DecimalField("temperature", 5.8)))

  "A GrokFilter" should {

    "return a MetaFilterDescriptor" in {
      GrokLogic.describe should not be null
    }

    "extract data into a new field when the grok rule matches the specified field" in new TestStream {
      whenReady(filterFuture) { filter =>

        source.sendNext(dataset1)

        sink.request(1)
        sink.expectNext(dataset1)

        Await.result(filter.configure(configurationB), 10 seconds)

        source.sendNext(dataset1)
        source.sendNext(dataset2)

        sink.requestNext().records should contain theSameElementsAs modified1.records
        sink.requestNext().records should contain theSameElementsAs modified2.records
      }
    }
  }
}
