package io.logbee.keyscore.pipeline.contrib.mqtt

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameParameter, ParameterSet, TextParameter}
import io.logbee.keyscore.model.data.{Dataset, Field, Record, TextValue}
import io.logbee.keyscore.pipeline.testkit.TestStreamForSink
import io.logbee.keyscore.test.fixtures.{ExampleData, TestSystemWithMaterializerAndExecutionContext}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DynamicMQTTSinkLogicSpec extends AnyWordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  implicit val defaultPatience = PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "A DynamicMQTTSinkLogic" should {

    val configuration = Configuration(
      parameterSet = ParameterSet(Seq(
        FieldNameParameter("mqtt.broker.fieldName", "broker"),
        FieldNameParameter("mqtt.topic.fieldName", "topic"),
        FieldNameParameter("mqtt.data.fieldName", "message")
      )
      ))

    "do some thing" in new TestStreamForSink[DynamicMQTTSinkLogic](configuration) {

      Await.ready(sinkFuture, 20 seconds)

      val d1 = Dataset(
        Record(
          Field("message", TextValue("This is the first record's message.")),
          Field("broker", TextValue("tcp://localhost:1883")),
          Field("topic", TextValue("/test/topic"))
        ),
        Record(
          Field("message", TextValue("This is the second record's message. It should not appear in the topic.")),
          Field("broker", TextValue("tcp://localhost:1889")),
          Field("topic", TextValue("/test/topic"))
        ),
        Record(
          Field("message", TextValue("This is the first record's message. It should not appear in the topic.")),
          Field("broker", TextValue("tcp://localhost:1883")),
          Field("topic", TextValue("/test/false"))
        ),
      )

      val d2 = Dataset(
        Record(
          Field("message", TextValue("This is the last record's message.")),
          Field("broker", TextValue("tcp://localhost:1883")),
          Field("topic", TextValue("/test/topic"))
        )
      )

      source.sendNext(d1)
      source.sendNext(d2)

      Thread.sleep(10000)
    }
  }
}
