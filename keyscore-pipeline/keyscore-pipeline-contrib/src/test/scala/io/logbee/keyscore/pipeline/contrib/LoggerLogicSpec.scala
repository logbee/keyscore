package io.logbee.keyscore.pipeline.contrib

import akka.actor.ActorSystem
import akka.testkit.EventFilter
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.pipeline.testkit.TestStreamForFilter
import io.logbee.keyscore.test.fixtures.ExampleData.{dataset1, messageTextField1}
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LoggerLogicSpec extends AnyFreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  override implicit val system: ActorSystem = ActorSystem("testsystem", ConfigFactory.parseString(
    """akka.loggers = ["akka.testkit.TestEventListener"]"""
  ))

  "A LoggerFilter" - {

    "should return a Descriptor" in {
      LoggerLogic.describe should not be null
    }

    "should log datasets" in new TestStreamForFilter[LoggerLogic](){

      whenReady(filterFuture) { filter =>

        EventFilter.info(pattern = s".*${messageTextField1.toTextField.value}.*", occurrences = 1) intercept {
          source.sendNext(dataset1)

          sink.request(1)
          sink.expectNext(dataset1)
        }
      }
    }
  }
}
