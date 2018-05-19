package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.UUID

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.agent.stream.ExampleData.{datasetMulti1, datasetMulti2, datasetMultiModified}
import io.logbee.keyscore.agent.stream.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.stream.stage.DefaultFilterStage
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, TextListParameter}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class RetainFieldsFilterFunctionSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {
  trait TestStream {
    val (filterFuture, probe) = Source(List(datasetMulti1, datasetMulti2))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val fieldsToRetain = TextListParameter("fieldsToRetain", List("42", "bbq"))
  val initialConfig = FilterConfiguration(UUID.randomUUID(), "fieldsToRetain", List(fieldsToRetain))

  val datasetMultiModified2 = Dataset(Record(TextField("42", "bar")))

  "A RetainFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      RetainFieldsFilterFunction.descriptors should not be null
    }

    "retain only the specified fields and remove all others" in new TestStream {
      whenReady(filterFuture) { filter =>
        val condition = stub[Condition]
        val retainFieldsFunction = stub[RetainFieldsFilterFunction]

        condition.apply _ when datasetMulti1 returns Accept(datasetMulti1)
        condition.apply _ when datasetMulti2 returns Reject(datasetMulti2)

        retainFieldsFunction.apply _ when datasetMulti1 returns datasetMultiModified
        retainFieldsFunction.apply _ when datasetMulti2 returns datasetMultiModified2

        Await.result(filter.changeCondition(condition), 10 seconds)
        Await.result(filter.changeFunction(retainFieldsFunction), 10 seconds)

        probe.request(2)
        probe.expectNext(datasetMultiModified)
        probe.expectNext(datasetMulti2)
      }
    }
  }
}
