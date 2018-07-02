package io.logbee.keyscore.agent.pipeline.valve

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.valve.ValvePosition.{Closed, Drain, Open}
import io.logbee.keyscore.model.Dataset
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ValveStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  class TestWithSourceProbeAndSinkProbe(bufferLimit: Int = 2) {
    val ((source, valveFuture), sink) = TestSource.probe[Dataset]
      .viaMat(new ValveStage(bufferLimit))(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  class TestWithSourceAndSinkProbe(bufferLimit: Int = 2, testData: List[Dataset]) {
    val (valveFuture, sink) = Source(List(testData:_*))
      .viaMat(new ValveStage(bufferLimit))(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A ValveStage" should {

    "pass through datasets" in new TestWithSourceAndSinkProbe(testData = List(dataset1, dataset2, dataset3)) {

      whenReady(valveFuture) { valve =>

        sink.request(3)

        sink.expectNext(dataset1)
        println(dataset1)
        sink.expectNext(dataset2)
        sink.expectNext(dataset3)
      }
    }

    "backpressure when closed, so only buffered messages pass through" in new TestWithSourceAndSinkProbe(bufferLimit = 2, testData = List(dataset1, dataset2, dataset3, dataset4)) {

      whenReady(valveFuture) { valve =>

        sink.request(1)
        sink.expectNext(dataset1)

        whenReady(valve.close()) { state =>
          state.position shouldBe Closed
          sink.request(3)
          sink.expectNext(dataset2)
          sink.expectNext(dataset3)
          sink.expectNoMessage(5 seconds)
        }
      }
    }

    "valve passes through datasets after it was opened again" in new TestWithSourceProbeAndSinkProbe {
      whenReady(valveFuture) { valve =>
        source.sendNext(dataset1)
        source.sendNext(dataset2)

        whenReady(valve.close()) { state =>
          state.position shouldBe Closed
          sink.request(1)
          sink.expectNoMessage(5 seconds)
        }

        whenReady(valve.open()) { state =>
          state.position shouldBe Open
        }

        sink.request(1)
        sink.expectNext(dataset1)

        sink.request(1)
        sink.expectNext(dataset2)
      }
    }

    "extracts single data from the RingBuffer" in new TestWithSourceProbeAndSinkProbe {
      whenReady(valveFuture) { valve =>
        whenReady(valve.insert(List(dataset1))) { state =>
          whenReady(valve.extract()) { datasets =>
            datasets should contain(dataset1)
          }
        }
      }
    }

    "extract n elements from the RingBuffer" in new TestWithSourceProbeAndSinkProbe {
      whenReady(valveFuture) { valve =>
        whenReady(valve.insert(List(dataset1, dataset2, dataset3))) { state =>
          whenReady(valve.extract(2)) { datasets =>
            datasets should contain inOrderOnly(dataset3, dataset2)
          }
        }
      }
    }

    "valve returns the complete state when state method is called" in new TestWithSourceProbeAndSinkProbe {
      whenReady(valveFuture) { valve =>

        Await.ready(valve.close(), 5 seconds)
        Await.ready(valve.open(), 5 seconds)

        whenReady(valve.state()) { state =>
          state.position shouldBe Open
        }
      }
    }

    "set the drain flag properly" in new TestWithSourceProbeAndSinkProbe {
      whenReady(valveFuture) { valve =>
        whenReady(valve.drain()) { state =>
          state.position shouldBe Drain
        }
      }
      whenReady(valveFuture) { valve =>
        whenReady(valve.open()) { state =>
          state.position shouldBe Open
        }
      }
    }
  }
}
