package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID
import java.util.UUID.randomUUID

import akka.stream.SourceShape
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.logbee.keyscore.commons.test.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
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

      val updateConfiguration = Promise[Configuration]
      val initializeConfiguration = Promise[Configuration]

      val uuid = randomUUID()
      val configurationA = Configuration()
      val configurationB = Configuration()
      val context = StageContext(system, executionContext)

      val provider = (parameters: LogicParameters, s: SourceShape[Dataset]) => new SourceLogic(LogicParameters(randomUUID(), context, configurationA), s) {

        override def initialize(configuration: Configuration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: Configuration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPull(): Unit = ???
      }

      val sourceFuture = Source.fromGraph(new SourceStage(LogicParameters(UUID.randomUUID(), context, configurationA), provider))
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
