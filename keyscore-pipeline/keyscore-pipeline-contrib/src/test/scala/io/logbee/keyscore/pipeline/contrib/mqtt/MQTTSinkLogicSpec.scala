package io.logbee.keyscore.pipeline.contrib.mqtt

import io.logbee.keyscore.model.configuration.{Configuration, FieldNameParameter, ParameterSet, TextParameter}
import io.logbee.keyscore.pipeline.testkit.TestStreamForSink
import io.logbee.keyscore.test.fixtures.{ExampleData, TestSystemWithMaterializerAndExecutionContext}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class MQTTSinkLogicSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  implicit val defaultPatience = PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "A MQTTSinkLogic" should {

    val configuration = Configuration(
      parameterSet = ParameterSet(Seq(
        TextParameter("mqtt.broker.url", "tcp://localhost:1883"),
        TextParameter("mqtt.topic", "/test/topic"),
        FieldNameParameter("mqtt.fieldName", "message")
      )
      ))

    "do some thing" in new TestStreamForSink[MQTTSinkLogic](configuration) {

      Await.ready(sinkFuture, 20 seconds)

      source.sendNext(ExampleData.dataset1)
      source.sendNext(ExampleData.dataset4)
      source.sendNext(ExampleData.dataset5)

      Thread.sleep(10000)
    }
  }
}