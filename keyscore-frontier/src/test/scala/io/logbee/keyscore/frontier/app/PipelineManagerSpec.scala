package io.logbee.keyscore.frontier.app

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import io.logbee.keyscore.frontier.app.PipelineManager.CreatePipeline
import io.logbee.keyscore.model.PipelineConfiguration
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class PipelineManagerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory  {

  val streamId = UUID.randomUUID()
  val sourceConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-source"))
  val sinkConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-sink"))

  "A PipelineManager should" should {
    "Send CreatePipeline Message only if available agents is not empty" in  {

      implicit val system = ActorSystem("test-system")
      val agentManager = TestProbe("agent-manager")
      val pipelineManager = system.actorOf(PipelineManager(agentManager.ref))

      val pipelineConfiguration = PipelineConfiguration(streamId,"test-configuration","A test pipeline", sourceConfiguration, List.empty, sinkConfiguration)

      pipelineManager ! CreatePipeline(pipelineConfiguration)

    }
  }

}
