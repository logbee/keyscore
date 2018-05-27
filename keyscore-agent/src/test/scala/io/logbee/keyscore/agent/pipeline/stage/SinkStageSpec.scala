package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID.randomUUID

import akka.stream.SinkShape
import akka.stream.scaladsl.{Keep, Source}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Promise
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class SinkStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A sink stage" should {

    "pass the appropriate configuration to it's logic" in {

      val updateConfiguration = Promise[FilterConfiguration]
      val initializeConfiguration = Promise[FilterConfiguration]

      val uuid = randomUUID()
      val descriptor = FilterDescriptor(randomUUID(), "testA")
      val configurationA = FilterConfiguration(uuid, descriptor, List.empty)
      val configurationB = FilterConfiguration(uuid, descriptor, List.empty)
      val context: StageContext = StageContext(system, executionContext)

      val provider = (ctx: StageContext, c: FilterConfiguration, s: SinkShape[Dataset]) => new SinkLogic(ctx, c, s) {

        override def initialize(configuration: FilterConfiguration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: FilterConfiguration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPush(): Unit = {

        }
      }

      val sinkFuture = Source(List(dataset1, dataset2))
        .toMat(new SinkStage(context, configurationA, provider))(Keep.right)
        .run()

      whenReady(sinkFuture) { sink =>
        sink.configure(configurationB)
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
