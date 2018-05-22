package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.actor.Props
import akka.testkit.TestProbe
import akka.util.Timeout
import io.logbee.keyscore.agent.stream.StreamManager.CreateStream
import io.logbee.keyscore.model.StreamConfiguration
import io.logbee.keyscore.model.filter.FilterConfiguration
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class StreamManagerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  implicit val timeout: Timeout = 30 seconds

  val streamId = UUID.randomUUID()

  "A StreamManager " should {

    "start a StreamSupervisor for a stream" in {

      val filterManagerProbe = TestProbe("filter-manager")
      val testee = system.actorOf(StreamManager(filterManagerProbe.ref), "stream-manager")

      val streamConfiguration = StreamConfiguration(streamId, "test", "A test stream.", FilterConfiguration(""), FilterConfiguration(""), List.empty)

      testee ! CreateStream(streamConfiguration)

//      whenReady(().mapTo[ActorRef]) { ref =>
//        whenReady((ref ? RequestStreamState).mapTo[StreamState]) { state =>
//          state shouldBe StreamState(streamId, Health.Green, streamConfiguration)
//        }
//
//        testee ! UpdateStream(streamConfiguration)
//      }
    }
  }
}
