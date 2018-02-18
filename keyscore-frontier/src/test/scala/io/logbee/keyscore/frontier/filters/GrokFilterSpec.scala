package io.logbee.keyscore.frontier.filters

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.model.{NumberField, TextField}
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._


class GrokFilterSpec extends WordSpec with ScalaFutures {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  val record1 = CommittableRecord(TextField("message", "The weather is cloudy with a current temperature of: -11.5 °C"))
  val record2 = CommittableRecord(TextField("message", "Is is a rainy day. Temperature: 5.8 °C"))
  val record3 = CommittableRecord(TextField("message", "The weather is sunny with a current temperature of: 14.4 °C"))

  val initialConfiguration = GrokFilterConfiguration(isPaused = false, fieldNames = List("message"), pattern = ".*:\\s(?<temperature>[-+]?\\d+((\\.\\d*)?|\\.\\d+)).*")

  "A paused grok filter" should {

    "only emit three records when it gets unpaused" in {

      val (handleFutur, probe) = Source(List(record1, record2, record3))
        .viaMat(GrokFilter(initialConfiguration.copy(isPaused = Some(true), pattern = Some(""))))(Keep.right)
        .toMat(TestSink.probe[CommittableRecord])(Keep.both)
        .run()

      whenReady(handleFutur.mapTo[GrokFilterHandle]) { handle =>

        probe.request(1)
        probe.expectNoMessage(100 milliseconds)

        whenReady(handle.configure(GrokFilterConfiguration(isPaused = false))) {
          _ shouldBe true
        }

        probe.expectNext(record1)

        probe.request(2)
        probe.expectNext(record2)
        probe.expectNext(record3)

        probe.expectComplete()
      }
    }
  }

  "An unpaused grok filter" should {


    "extract data into a new field when the grok rule matches the specified field" in {

      val (handleFutur, probe) = Source(List(record1, record2, record3))
        .viaMat(GrokFilter(initialConfiguration))(Keep.right)
        .toMat(TestSink.probe[CommittableRecord])(Keep.both)
        .run()

      whenReady(handleFutur.mapTo[GrokFilterHandle]) { handle =>

        probe.request(3)

        probe.expectNext(CommittableRecord(record1, NumberField("temperature", -11.5)))
        probe.expectNext(CommittableRecord(record2, NumberField("temperature", 5.8)))
        probe.expectNext(CommittableRecord(record3, NumberField("temperature", 14.4)))
        probe.expectComplete()
      }
    }

    "not extract any data if the list of field names is empty" in {

      val (handleFutur, probe) = Source(List(record1, record2, record3))
        .viaMat(GrokFilter(initialConfiguration.copy(fieldNames = Some(List.empty))))(Keep.right)
        .toMat(TestSink.probe[CommittableRecord])(Keep.both)
        .run()

      whenReady(handleFutur.mapTo[GrokFilterHandle]) { handle =>

        probe.request(3)

        probe.expectNext(record1)
        probe.expectNext(record2)
        probe.expectNext(record3)
        probe.expectComplete()
      }
    }
  }
}
