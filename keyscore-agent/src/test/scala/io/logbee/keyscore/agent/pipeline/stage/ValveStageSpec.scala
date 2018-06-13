package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
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

  trait TestWithSinkandSource {
    val ((source, valveFuture), sink) = TestSource.probe[Dataset]
      .viaMat(new ValveStage())(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  trait TestWithSimpleSource {
    val (valveFuture, sink) = Source(List(dataset3, dataset3, dataset3,
      dataset3, dataset3, dataset3, dataset3, dataset3, dataset3, dataset3, dataset3, dataset3))
      .viaMat(new ValveStage())(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }


  "A ValveStage" should {
    "passes through datasets" in new TestWithSinkandSource {

      whenReady(valveFuture) { valveProxy =>

        source.sendNext(dataset1)
        source.sendNext(dataset2)
        source.sendNext(dataset3)

        sink.request(3)

        sink.expectNext(dataset1)
        sink.expectNext(dataset2)
        sink.expectNext(dataset3)
      }
    }

    "when closed no Messages pass through the valve" in new TestWithSinkandSource {

      whenReady(valveFuture) { valveProxy =>
        source.sendNext(dataset1)

        whenReady(valveProxy.pause()) { state =>
          state.isPaused shouldBe true
          sink.request(1)
          sink.expectNoMessage(5 seconds)
        }
      }
    }

    "valve passes through datasets after it was opened again" in new TestWithSinkandSource {
      whenReady(valveFuture) { valveProxy =>
        source.sendNext(dataset1)
        source.sendNext(dataset2)

        whenReady(valveProxy.pause()) { state =>
          state.isPaused shouldBe true
          sink.request(1)
          sink.expectNoMessage(5 seconds)
        }

        whenReady(valveProxy.unpause()) { state =>
          state.isPaused shouldBe false
        }
        sink.request(1)
        sink.expectNext(dataset1)

        sink.request(1)
        sink.expectNext(dataset2)
      }
    }

    "extracts single data from the Buffer" in new TestWithSinkandSource {
      whenReady(valveFuture) { valveProxy =>
        whenReady(valveProxy.insert(dataset1)) { state =>
          whenReady(valveProxy.extractDatasets()) { datasets =>
            datasets should contain(dataset1)
          }
        }
      }
    }
    "extract n elements from the Buffer" in new TestWithSinkandSource {
      whenReady(valveFuture) { valveProxy =>
        whenReady(valveProxy.insert(dataset1, dataset2, dataset3)) { state =>
          whenReady(valveProxy.extractDatasets(2)) { datasets =>
            datasets should contain inOrderOnly (dataset3, dataset2)
          }
        }
      }
    }

    "valve returns the complete state when state method is called" in new TestWithSinkandSource {
      whenReady(valveFuture) { valveProxy =>

        Await.ready(valveProxy.pause(), 5 seconds)
        Await.ready(valveProxy.unpause(), 5 seconds)

        whenReady(valveProxy.state()) { state =>
          state.isPaused shouldBe false
        }
      }
    }

    "set the drain flag properly" in new TestWithSinkandSource  {
      whenReady(valveFuture) { valveProxy =>
        whenReady(valveProxy.allowDrain(true)) { state =>
          state.allowDrain shouldBe true
        }
      }
      whenReady(valveFuture) { valveProxy =>
        whenReady(valveProxy.allowDrain(false)) { state =>
          state.allowDrain shouldBe false
        }
      }
    }
  }
}
