package io.logbee.keyscore.agent.stream.contrib.filter

import java.util.UUID

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.agent.stream.ExampleData.{dataset1, dataset2, dataset3}
import io.logbee.keyscore.agent.stream.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.stream.stage.DefaultFilterStage
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextListParameter, TextParameter}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class GrokFilterFunctionSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestStream {
    val (filterFuture, probe) = Source(List(dataset1, dataset2, dataset3))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val fieldNames = TextListParameter("fieldNames", List("message"))
  val pattern = TextParameter("pattern", ".*:\\s(?<temperature>[-+]?\\d+((\\.\\d*)?|\\.\\d+)).*")
  val parameterList = List(fieldNames, pattern)

  val modified1 = Dataset(Record(NumberField("temperature", -11.5)))
  val modified2 = Dataset(Record(NumberField("temperature", 5.8)))
  val modified3 = Dataset(Record(NumberField("temperature", 14.4)))

  val initialConfig = FilterConfiguration(UUID.randomUUID(), FilterDescriptor(UUID.randomUUID(), "addFieldsFilter"), parameterList)

  "A GrokFilter" should {

    "return a MetaFilterDescriptor" in {
      GrokFilterFunction.describe should not be null
    }

    "extract data into a new field when the grok rule matches the specified field" in new TestStream {
      whenReady(filterFuture) { filter =>

        val condition = stub[Condition]
        val grokFunction = stub[GrokFilterFunction]

        condition.apply _ when dataset1 returns Accept(dataset1)
        condition.apply _ when dataset2 returns Reject(dataset2)
        condition.apply _ when dataset3 returns Accept(dataset3)

        grokFunction.apply _ when dataset1 returns modified1
        grokFunction.apply _ when dataset2 returns modified2
        grokFunction.apply _ when dataset3 returns modified3

        Await.result(filter.changeCondition(condition), 10 seconds)
        Await.result(filter.changeFunction(grokFunction), 10 seconds)

        probe.request(3)
        probe.expectNext(modified1)
        probe.expectNext(dataset2)
        probe.expectNext(modified3)
      }
    }
  }
}
