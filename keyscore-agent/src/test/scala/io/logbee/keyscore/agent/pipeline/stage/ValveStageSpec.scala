package io.logbee.keyscore.agent.pipeline.stage

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.Dataset
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
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

//    "when closed no Messages pass through the valve" in new TestWithSinkandSource {
//
//      whenReady(valveFuture) { valveProxy =>
//        source.sendNext(dataset1)
//
//        whenReady(valveProxy.pause()) { state =>
//          state.isPaused shouldBe true
//          sink.request(1)
//          sink.expectNoMessage(5 seconds)
//        }
//      }
//    }
//
//    "valve passes through datasets after it was opened again" in new TestWithSinkandSource {
//      whenReady(valveFuture) { valveProxy =>
//        source.sendNext(dataset1)
//        source.sendNext(dataset2)
//
//        whenReady(valveProxy.pause()) { state =>
//          state.isPaused shouldBe true
//          sink.request(1)
//          sink.expectNoMessage(5 seconds)
//        }
//
//        whenReady(valveProxy.unpause()) { state =>
//          state.isPaused shouldBe false
//        }
//        sink.request(1)
//        sink.expectNext(dataset1)
//
//        sink.request(1)
//        sink.expectNext(dataset2)
//      }
//    }

//    "valve saves the dataset that passed through" in new TestWithSinkandSource {
//      whenReady(valveFuture) { valveProxy =>
//        source.sendNext(dataset1)
//        source.sendNext(dataset2)
//        source.sendNext(dataset3)
//
//     ValveStage   sink.request(3)
//
//        whenReady(valveProxy.extractDatasets()) { datasets =>
//          datasets should have size 1
//          datasets should contain(dataset3)
//        }
//
//        whenReady(valveProxy.extractDatasets(3)) { datasets =>
//          datasets should have size 3
//          datasets should contain inOrder(dataset1, dataset2, dataset3)
//        }
//      }
//    }

    "valve buffers the last 10 datasets" in new TestWithSinkandSource {
      whenReady(valveFuture) { valveProxy =>
        source.sendNext(dataset1)
        source.sendNext(dataset1)
        source.sendNext(dataset1)
        source.sendNext(dataset1)
        source.sendNext(dataset1)


        sink.request(5)

        whenReady(valveProxy.extractDatasets()) { dataset =>
          dataset should have size 1
          dataset.head shouldBe dataset1
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
//    "valve pushes out inserted dataset" in new TestWithSinkandSource {
//      whenReady(valveFuture) { valveProxy =>
//
//        sink.request(1)
//        whenReady(valveProxy.insert(dataset1)) { x =>
//          sink.expectNext(dataset1)
//        }
//
//      }
//    }

//    "valve pushes nothing when allowDrain is false" in new TestWithSinkandSource {
//      whenReady(valveFuture) { valveProxy =>
//        Await.ready(valveProxy.pause(), 1 seconds)
//        Await.ready(valveProxy.allowPull(), 1 seconds)
//        whenReady(valveProxy.allowDrain()) { x =>
//          source.sendNext(dataset1)
//          Await.ready(valveProxy.pause(), 1 seconds)
//          sink.request(1)
//          sink.expectNoMessage(1 seconds)
//        }
//      }
//    }
  }


}
