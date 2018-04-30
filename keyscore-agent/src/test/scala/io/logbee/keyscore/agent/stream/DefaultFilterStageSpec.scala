package io.logbee.keyscore.agent.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.{Dataset, Record, TextField}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DefaultFilterStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  val record1 = Record(TextField("message", "The weather is cloudy with a current temperature of: -11.5 °C"))
  val record2 = Record(TextField("message", "Is is a rainy day. Temperature: 5.8 °C"))
  val record1Modified = Record(TextField("weather-report", "cloudy, -11.5 °C"))
  val record2Modified = Record(TextField("weather-report", "rainy, 5.8 °C"))

  val dataset1 = Dataset(record1)
  val dataset2 = Dataset(record2)
  val dataset1Modified = Dataset(record1Modified)
  val dataset2Modified = Dataset(record2Modified)

  trait TestStream {
    val (handleFutur, probe) = Source(List(dataset1, dataset2))
      .viaMat(new DefaultFilterStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A filter" should {

    "pass elements unmodified, if no filter and condition is specified" in new TestStream {

      whenReady(handleFutur) { filter =>
        probe.request(2)
        probe.expectNext(dataset1)
        probe.expectNext(dataset2)
      }
    }

    "pass elements unmodified, if the configured condition does not accept the elements" in new TestStream {

      whenReady(handleFutur) { filter =>

        val condition = stub[FilterCondition]
        condition.apply _ when dataset1 returns Reject(dataset1)
        condition.apply _ when dataset2 returns Reject(dataset2)

        whenReady(filter.changeCondition(condition)) { result =>
          probe.request(2)
          probe.expectNext(dataset1)
          probe.expectNext(dataset2)
        }
      }
    }

    "pass a element modified, if the configured condition accepts it" in new TestStream {

      whenReady(handleFutur) { filter =>

        val condition = stub[FilterCondition]
        val function = stub[FilterFunction]

        condition.apply _ when dataset1 returns Accept(dataset1)
        condition.apply _ when dataset2 returns Reject(dataset2)

        function.apply _ when dataset1 returns dataset1Modified
        function.apply _ when dataset2 returns dataset2Modified

        Await.result(filter.changeCondition(condition), 10 seconds) shouldBe true
        Await.result(filter.changeFunction(function), 10 seconds) shouldBe true

        probe.request(2)
        probe.expectNext(dataset1Modified)
        probe.expectNext(dataset2)
      }
    }

    "pass an updated configuration to the condition and function" in new TestStream {

      whenReady(handleFutur) { filter =>

        val condition = stub[FilterCondition]
        val function = stub[FilterFunction]
        val conditionConfiguration = FilterConfiguration(null, "test", List.empty)
        val functionConfiguration = FilterConfiguration(null, "test", List.empty)

        Await.result(filter.changeCondition(condition), 30 seconds) shouldBe true
        Await.result(filter.changeFunction(function), 30 seconds) shouldBe true
        Await.result(filter.configureFunction(functionConfiguration), 30 seconds) shouldBe true
        Await.result(filter.configureCondition(conditionConfiguration), 30 seconds) shouldBe true

        condition.configure _ verify conditionConfiguration
        function.configure _ verify functionConfiguration
      }
    }
  }
}