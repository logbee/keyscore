package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.{ActorMaterializer, SinkShape}
import com.typesafe.config.ConfigFactory
import io.logbee.keyscore.agent.stream.ExampleData._
import io.logbee.keyscore.agent.stream.contrib.stages.{SinkLogic, SinkStage}
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
class SinkStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory {

  private val config = ConfigFactory.load()
  implicit val system = ActorSystem("keyscore", config.getConfig("test").withFallback(config))
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = materializer.executionContext

  "A sink stage" should {

    "pass the appropriate configuration to it's logic" in {

      val updateConfiguration = Promise[FilterConfiguration]
      val initializeConfiguration = Promise[FilterConfiguration]

      val uuid = UUID.randomUUID()
      val configurationA = FilterConfiguration(uuid, "test", List.empty)
      val configurationB = FilterConfiguration(uuid, "test", List.empty)
      val provider = (c: FilterConfiguration, s: SinkShape[Dataset]) => new SinkLogic(c, s) {

        override def initialize(configuration: FilterConfiguration): Unit = {
          initializeConfiguration.success(configuration)
        }

        override def configure(configuration: FilterConfiguration): Unit = {
          updateConfiguration.success(configuration)
        }

        override def onPush(): Unit = ???
      }

      val sinkFuture = Source(List(dataset1, dataset2))
        .toMat(new SinkStage(provider, configurationA))(Keep.right)
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
