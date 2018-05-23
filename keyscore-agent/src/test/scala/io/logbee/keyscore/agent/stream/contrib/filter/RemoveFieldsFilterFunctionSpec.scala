package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.UUID.randomUUID

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.agent.stream.ExampleData.{datasetMulti1, datasetMulti2, datasetMultiModified, datasetMultiModified2}
import io.logbee.keyscore.agent.stream.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.stream.stage.DefaultFilterStage
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextListParameter}
import io.logbee.keyscore.model.{Accept, Condition, Dataset, Reject}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

/*
@RunWith(classOf[JUnitRunner])
class RemoveFieldsFilterFunctionSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {
    val (filterFuture, probe) = Source(List(datasetMulti1, datasetMulti2))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val fieldsToRemove = TextListParameter("fieldsToRemove", List("foo", "beer"))
  val initialConfig = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "removeFieldsFilter"), List(fieldsToRemove))

  "A RemoveFieldsFilter" should {

    "return a MetaFilterDescriptor" in {
      RemoveFieldsFilterFunction.describe should not be null
    }

    "remove specified fields" in new TestStream {
      whenReady(filterFuture) { filter =>
        val condition = stub[Condition]
        val removeFieldsFunction = stub[RemoveFieldsFilterFunction]

        condition.apply _ when datasetMulti1 returns Accept(datasetMulti1)
        condition.apply _ when datasetMulti2 returns Reject(datasetMulti2)

        removeFieldsFunction.apply _ when datasetMulti1 returns datasetMultiModified
        removeFieldsFunction.apply _ when datasetMulti2 returns datasetMultiModified2

        Await.result(filter.changeCondition(condition), 10 seconds)
        Await.result(filter.changeFunction(removeFieldsFunction), 10 seconds)

        probe.request(2)
        probe.expectNext(datasetMultiModified)
        probe.expectNext(datasetMulti2)
      }
    }
  }
}
*/