package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.testkit.TestProbe
import akka.util.Timeout
import io.logbee.keyscore.commons.test.ProductionSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.configuration.Configuration
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class LocalPipelineManagerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with ProductionSystemWithMaterializerAndExecutionContext {

  implicit val timeout: Timeout = 30 seconds

  val streamId = UUID.randomUUID()
  val sourceConfiguration = Configuration()
  val sinkConfiguration = Configuration()

  "A LocalPipelineManager " should {

    "start a StreamSupervisor for a pipeline" in {

      val filterManagerProbe = TestProbe("filter-manager")
      val testee = system.actorOf(LocalPipelineManager(filterManagerProbe.ref), "LocalPipelineManager")

      val streamConfiguration = PipelineConfiguration(streamId, "test", "A test pipeline.", sourceConfiguration, List.empty, sinkConfiguration)

//      testee ! CreatePipeline(streamConfiguration)

//      whenReady(().mapTo[ActorRef]) { ref =>
//        whenReady((ref ? RequestPipelineInstance).mapTo[PipelineInstance]) { state =>
//          state shouldBe PipelineInstance(streamId, Health.Green, streamConfiguration)
//        }
//
//        testee ! UpdatePipeline(streamConfiguration)
//      }
    }
  }
}
