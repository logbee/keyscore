package io.logbee.keyscore.example.filter

import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.testkit.{TestActorSystem, TestStreamForFilter}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ExampleLogicSpec extends FreeSpec with Matchers with ScalaFutures with TestActorSystem {

  val sample = Dataset(records = Record(
    Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C")),
  ))

  "A ExampleLogic" - {

    "should pass through datasets" in new TestStreamForFilter[ExampleLogic]() {

      whenReady(filterFuture) { _ =>

        source.sendNext(sample)

        sink.requestNext() shouldBe sample

      }
    }
  }
}
