package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.testkit.TestProbe
import io.logbee.keyscore.agent.stream.StreamSupervisor.CreateStream
import io.logbee.keyscore.model.StreamConfiguration
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class StreamSupervisorSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  "A running StreamSupervisor" should {

    val filterManager = TestProbe()

    val streamId = UUID.randomUUID()
    val sourceConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-source"))
    val sinkConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-sink"))
    val streamConfiguration = StreamConfiguration(streamId, "test", "A test stream.", sourceConfiguration, List.empty, sinkConfiguration)

    val supervisor = system.actorOf(StreamSupervisor(filterManager.ref))

    "fubar" in {
      supervisor ! CreateStream(streamConfiguration)

      Thread.sleep(10000)
    }
  }
}
