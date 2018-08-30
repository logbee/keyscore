package io.logbee.keyscore.agent.pipeline.valve

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.agent.pipeline.valve.ValvePosition.{Closed, Drain, Open}
import io.logbee.keyscore.model.data._
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ValveStageSpec extends WordSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  class TestWithSourceProbeAndSinkProbe(bufferLimit: Int = 2) {
    val ((source, valveFuture), sink) = TestSource.probe[Dataset]
      .viaMat(new ValveStage(bufferLimit))(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  class TestWithSourceAndSinkProbe(bufferLimit: Int = 2, testData: List[Dataset]) {
    val (valveFuture, sink) = Source(List(testData: _*))
      .viaMat(new ValveStage(bufferLimit))(Keep.right)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A ValveStage" should {

    "pass through datasets" in new TestWithSourceAndSinkProbe(testData = List(dataset1, dataset2, dataset3)) {

      whenReady(valveFuture) { valve =>

        sink.request(3)

        sink.requestNext().records should contain theSameElementsAs dataset1.records
        sink.requestNext().records should contain theSameElementsAs dataset2.records
        sink.requestNext().records should contain theSameElementsAs dataset3.records
      }
    }

    "backpressure when closed, so only buffered messages pass through" in new TestWithSourceAndSinkProbe(bufferLimit = 2, testData = List(dataset1, dataset2, dataset3, dataset4)) {

      whenReady(valveFuture) { valve =>

        sink.requestNext().records should contain theSameElementsAs dataset1.records

        whenReady(valve.close()) { state =>

          state.position shouldBe Closed

          sink.request(3)

          sink.requestNext().records should contain theSameElementsAs dataset2.records
          sink.requestNext().records should contain theSameElementsAs dataset3.records

          sink.expectNoMessage(5 seconds)
        }
      }
    }

    "let datasets pass through after it was opened again" in new TestWithSourceProbeAndSinkProbe {
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

        sink.requestNext().records should contain theSameElementsAs dataset1.records
        sink.requestNext().records should contain theSameElementsAs dataset2.records
      }
    }

    "drops datasets when drained" in new TestWithSourceProbeAndSinkProbe {

      whenReady(valveFuture) { valve =>

        whenReady(valve.drain()) { state =>

          state.position shouldBe Drain

          source.sendNext(dataset1)
          source.sendNext(dataset2)

          sink.request(1)
          sink.expectNoMessage(5 seconds)

          whenReady(valve.open()) { state =>

            state.position shouldBe Open

            source.sendNext(dataset3)
            source.sendNext(dataset4)

            sink.requestNext().records should contain theSameElementsAs dataset3.records
            sink.requestNext().records should contain theSameElementsAs dataset4.records
          }
        }
      }
    }

    "retains the labels of dataset" in new TestWithSourceProbeAndSinkProbe {

      whenReady(valveFuture) { valve =>

        val expectedLabel = Label("foo", TextValue("bar"))
        val dataset = Dataset(MetaData(expectedLabel), Record(Field("message", TextValue("Hello World!"))))

        source.sendNext(dataset)
        sink.requestNext().metadata.labels should contain (expectedLabel)
      }
    }

    "returns the complete state when state method is called" in new TestWithSourceProbeAndSinkProbe {
      whenReady(valveFuture) { valve =>

        Await.ready(valve.close(), 5 seconds)
        Await.ready(valve.open(), 5 seconds)

        whenReady(valve.state()) { state =>
          state.position shouldBe Open
          state.throughputTime should equal(0)
          state.totalThroughputTime should equal(0)
          state.bufferLimit shouldBe a[Integer]
          state.bufferSize shouldBe a[Integer]
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
