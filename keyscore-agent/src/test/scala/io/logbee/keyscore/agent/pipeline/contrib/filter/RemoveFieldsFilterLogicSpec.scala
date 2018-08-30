package io.logbee.keyscore.agent.pipeline.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData.{datasetMulti1, datasetMultiModified, datasetMultiModified2}
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilterLogic.fieldsToRemoveParameter
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, LogicParameters, StageContext}
import io.logbee.keyscore.model.configuration.{Configuration, TextListParameter}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor.ToParameterRef.toRef
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class RemoveFieldsFilterLogicSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {

    val context = StageContext(system, executionContext)
    val provider = (ctx: StageContext, c: Configuration, s: FlowShape[Dataset, Dataset]) => new RemoveFieldsFilterLogic(LogicParameters(randomUUID(), ctx, c), s)

    val config1 = Configuration(
      TextListParameter(fieldsToRemoveParameter, Seq("bbq", "beer","bar","notPresent"))
    )

    val config2 = Configuration(
      TextListParameter("fieldsToRemove", List("foo", "beer","beer"))
    )

    val filterStage = new FilterStage(context, config1, provider)

    val ((source, filterFuture), sink) = Source.fromGraph(TestSource.probe[Dataset])
      .viaMat(filterStage)(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A RemoveFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      RemoveFieldsFilterLogic.describe should not be null
    }

    "remove specified fields" in new TestStream {
      whenReady(filterFuture) { filter =>
        source.sendNext(datasetMulti1)
        sink.request(1)
        sink.expectNext(datasetMultiModified)

        Await.ready(filter.configure(config2),10 seconds)

        source.sendNext(datasetMulti1)
        sink.request(1)
        sink.expectNext(datasetMultiModified2)
      }
    }
  }
}