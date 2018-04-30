package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.agent.stream.ExampleData.{dataset1, dataset2, dataset3}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, TextMapParameter}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._
import java.util.UUID

import io.logbee.keyscore.agent.stream.contrib.GrokFilterFunction

import scala.concurrent.Await

class AddFieldsFilterFunctionSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  trait TestStream {
    val (filterFuture, probe) = Source(List(dataset1, dataset2, dataset3))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  var map = Map[String, String]("foo" -> "bier", "42" -> "73")
  val fieldsToAdd = TextMapParameter("fieldsToAdd", map)
  val initialConfig = FilterConfiguration(UUID.randomUUID(), "addFieldsFilter", List(fieldsToAdd))

  val modified1 = Dataset(Record(
    TextField("message", "The weather is cloudy with a current temperature of: -11.5 °C"),
    TextField("foo", "bier"),
    TextField("42", "73")
  ))

  val modified2 = Dataset(Record(
    TextField("message", "Is is a rainy day. Temperature: 5.8 °C"),
    TextField("foo", "bier"),
    TextField("42", "73")
  ))

  "A AddFieldsFilter" should {
    "add new fields and their data to the already existing data" in new TestStream {
      whenReady(filterFuture) { filter =>
        val condition = stub[Condition]
        val grokFunction = stub[GrokFilterFunction]

        condition.apply _ when dataset1 returns Accept(dataset1)
        condition.apply _ when dataset2 returns Reject(dataset2)

        grokFunction.apply _ when dataset1 returns modified1
        grokFunction.apply _ when dataset2 returns modified2

        Await.result(filter.changeCondition(condition), 10 seconds) shouldBe true
        Await.result(filter.changeFunction(grokFunction), 10 seconds) shouldBe true

        probe.request(2)
        probe.expectNext(modified1)
        probe.expectNext(dataset2)
      }

    }
  }
}
