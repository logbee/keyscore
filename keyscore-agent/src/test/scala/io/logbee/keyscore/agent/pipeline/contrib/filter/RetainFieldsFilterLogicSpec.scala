package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID
import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.{datasetMulti1, datasetMultiModified, datasetMultiModified2}
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, LogicParameters, StageContext}
import io.logbee.keyscore.commons.test.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.configuration.{Configuration, TextListParameter}
import io.logbee.keyscore.model.data.Dataset
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class RetainFieldsFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)
    val provider = (ctx: StageContext, c: Configuration, s: FlowShape[Dataset, Dataset]) =>
      new RetainFieldsFilterLogic(LogicParameters(UUID.randomUUID(),ctx, c), s)

    val config1 = Configuration(
      parameters = Seq(
        TextListParameter(RetainFieldsFilterLogic.fieldNamesParameter.ref,Seq("bar", "bbq"))
      )
    )

    val config2 = Configuration(
      parameters = Seq(
        TextListParameter(RetainFieldsFilterLogic.fieldNamesParameter.ref,Seq("foo", "notPresent"))
      )
    )

    val filterStage = new FilterStage(context,config1,provider)

   val ((source,filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A RetainFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      RetainFieldsFilterLogic.describe should not be null
    }

    "retain only the specified fields and remove all others" in new TestStream {
      whenReady(filterFuture) { filter =>
        source.sendNext(datasetMulti1)
        sink.request(1)
        sink.expectNext(datasetMultiModified)

        Await.ready(filter.configure(config2), 10 seconds)

        source.sendNext(datasetMulti1)
        sink.request(1)
        sink.expectNext(datasetMultiModified2)

      }
    }
  }
}
