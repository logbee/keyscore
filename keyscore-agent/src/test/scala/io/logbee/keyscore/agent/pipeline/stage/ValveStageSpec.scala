package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.Dataset
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.language.postfixOps

class ValveStageSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestWithSinkandSource {
    val ((source, valveFuture), sink) = TestSource.probe[Dataset]
      .viaMat(new ValveStage())(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  trait TestWithSimpleSource {
    val (valveFuture,sink) = Source(List(dataset3, dataset3, dataset3,
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

        sink.requestNext(dataset1)
        sink.requestNext(dataset2)
        sink.requestNext(dataset3)
      }
    }

    "when closed no Messages pass through the valve" in new TestWithSinkandSource {

      whenReady(valveFuture) { valveProxy =>
        source.sendNext(dataset1)

        whenReady(valveProxy.close()) { state =>
          println("closed valve")
          sink.request(1)
          sink.expectNoMessage(5 seconds)
        }
      }
    }

    "valve passes through datasets after it was opened again" in new TestWithSinkandSource {
      whenReady(valveFuture) { valveProxy =>
        source.sendNext(dataset1)
        source.sendNext(dataset2)

        whenReady(valveProxy.close()) { state =>
          println("closed valve")
          sink.request(1)
          sink.expectNoMessage(5 seconds)
        }

        whenReady(valveProxy.open()) { state =>
          println("opened valve")
        }
        sink.request(1)
        sink.expectNext(dataset1)

        sink.request(1)
        sink.expectNext(dataset2)
      }
    }

    "valve saves the  dataset thaT passed through" in new TestWithSinkandSource {
      whenReady(valveFuture) { valveProxy =>
        source.sendNext(dataset1)
        source.sendNext(dataset2)
        source.sendNext(dataset3)

        sink.request(3)

        whenReady(valveProxy.last()) { datasets =>
          datasets should have size 1
          datasets should contain(dataset3)
        }

        whenReady(valveProxy.last(3)) { datasets =>
          datasets should have size 3
          datasets should contain inOrder(dataset1, dataset2, dataset3)
        }
      }
    }


    "valve buffers the last 10 datasets" in new TestWithSimpleSource {
      whenReady(valveFuture) { valve =>
        sink.request(11)

        whenReady(valve.last(11)) { datasets =>
          datasets should have size 10
        }

      }
    }
  }

}
