package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.agent.stream.ExampleData.{datasetMulti, datasetMulti2, datasetMultiModified}
import io.logbee.keyscore.agent.stream.contrib.RetainFieldsFilterFunction
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, TextListParameter}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._
import java.util.UUID

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith

import scala.concurrent.Await
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RetainFieldsFilterFunctionSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {

  private val config = ConfigFactory.load()
  implicit val system = ActorSystem("keyscore", config.getConfig("test").withFallback(config))
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  trait TestStream {
    val (filterFuture, probe) = Source(List(datasetMulti, datasetMulti2))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  val fieldsToRetain = TextListParameter("fieldsToRetain", List("42", "bbq"))
  val initialConfig = FilterConfiguration(UUID.randomUUID(), "fieldsToRetain", List(fieldsToRetain))

  val datasetMultiModified2 = Dataset(Record(TextField("42", "bar")))

  "A RetainFieldsFilter" should {
    "retain only the specified fields and remove all others" in new TestStream {
      whenReady(filterFuture) { filter =>
        val condition = stub[Condition]
        val retainFieldsFunction = stub[RetainFieldsFilterFunction]

        condition.apply _ when datasetMulti returns Accept(datasetMulti)
        condition.apply _ when datasetMulti2 returns Reject(datasetMulti2)

        retainFieldsFunction.apply _ when datasetMulti returns datasetMultiModified
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
