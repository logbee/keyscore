package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.stream.SourceShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.FilterConfiguration
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Promise
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class SourceStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A source stage" should {

    "pass the appropriate configuration to it's logic" in {

      val updateConfiguration = Promise[FilterConfiguration]
      val initializeConfiguration = Promise[FilterConfiguration]

      val uuid = UUID.randomUUID()
      val configurationA = FilterConfiguration(uuid, "testA", List.empty)
      val configurationB = FilterConfiguration(uuid, "testB", List.empty)
      val provider = (c: FilterConfiguration, s: SourceShape[Dataset]) => new SourceLogic(c, s) {

        override def initialize(configuration: FilterConfiguration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: FilterConfiguration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPull(): Unit = ???
      }

      val sourceFuture = Source.fromGraph(new SourceStage(provider, configurationA))
        .toMat(TestSink.probe[Dataset])(Keep.left)
        .run()

      whenReady(sourceFuture) { source =>
        source.configure(configurationB)
      }

      whenReady(initializeConfiguration.future) { success =>
        success shouldBe configurationA
      }

      whenReady(updateConfiguration.future) { success =>
        success shouldBe configurationB
      }
    }
  }
}
