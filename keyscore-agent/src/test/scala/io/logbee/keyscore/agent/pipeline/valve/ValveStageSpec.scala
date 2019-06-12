package io.logbee.keyscore.agent.pipeline.valve

import akka.actor.ActorRef
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestProbe
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor._
import io.logbee.keyscore.agent.pipeline.valve.ValvePosition.{Closed, Drain, Open}
import io.logbee.keyscore.agent.pipeline.valve.ValveStage._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.test.fixtures.ToActorRef.Probe2ActorRef
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ValveStageSpec extends FreeSpec with Matchers with ScalaFutures with TestSystemWithMaterializerAndExecutionContext {

  class TestWithSourceProbeAndSinkProbe(supervisor: ActorRef = system.deadLetters, bufferLimit: Int = 2) {
    val ((source, valveFuture), sink) = TestSource.probe[Dataset]
      .viaMat(new ValveStage(supervisor, bufferLimit))(Keep.both)
      .toMat(TestSink.probe[Dataset])(Keep.both)
      .run()
  }

  "A ValveStage" - {

    "on completion" - {

      val supervisor = TestProbe()

      "should send StageCompleted message" in new TestWithSourceProbeAndSinkProbe(supervisor) {

        source.sendComplete()
        val message = supervisor.receiveOne(5 seconds).asInstanceOf[StageCompleted]
        message.id should not be null
      }
    }

    "on failure" - {

      val supervisor = TestProbe()

      "should send StageFailure message" in new TestWithSourceProbeAndSinkProbe(supervisor) {

        source.sendError(new Exception("Hammer to fall!"))
        val message = supervisor.receiveOne(5 seconds).asInstanceOf[StageFailed]
        message.id should not be null

      }
    }

    s"should pass through datasets and increase ${pushedDatasets.name}" in new TestWithSourceProbeAndSinkProbe {

      whenReady(valveFuture) { valve =>
        val wantedDatasets = 3

        source.sendNext(dataset1)
        source.sendNext(dataset2)
        source.sendNext(dataset3)

        sink.request(wantedDatasets)

        sink.requestNext().records should contain theSameElementsAs dataset1.records
        sink.requestNext().records should contain theSameElementsAs dataset2.records
        sink.requestNext().records should contain theSameElementsAs dataset3.records

        whenReady(valve.state()) { state =>
          val id = state.id

          whenReady(valve.scrape()) { mc =>
            mc.find(pushedDatasets).get.value should equal(wantedDatasets.toDouble)
          }
        }
      }
    }

    "should backpressure when closed, so only buffered messages pass through"  in new TestWithSourceProbeAndSinkProbe(bufferLimit = 2) {

      whenReady(valveFuture) { valve =>

        source.sendNext(dataset1)
        source.sendNext(dataset2)
        source.sendNext(dataset3)
        source.sendNext(dataset4)

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

    "should let datasets pass through after it was opened again" in new TestWithSourceProbeAndSinkProbe {

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

    "should drop datasets when drained" in new TestWithSourceProbeAndSinkProbe {

      whenReady(valveFuture) { valve =>

        source.sendNext(dataset1)
        source.sendNext(dataset2)

        sink.requestNext().records should contain theSameElementsAs dataset1.records
        sink.requestNext().records should contain theSameElementsAs dataset2.records

        whenReady(valve.drain()) { state =>

          state.position shouldBe Drain

          source.sendNext(dataset1)
          source.sendNext(dataset2)

          sink.request(2)
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

    "should retains the labels of dataset" in new TestWithSourceProbeAndSinkProbe {

      whenReady(valveFuture) { valve =>

        val expectedLabel = Label("foo", TextValue("bar"))
        val dataset = Dataset(MetaData(expectedLabel), Record(Field("message", TextValue("Hello World!"))))

        source.sendNext(dataset)
        sink.requestNext().metadata.labels should contain(expectedLabel)
      }
    }

    "should returns the complete state when state method is called" in new TestWithSourceProbeAndSinkProbe {
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

    "should set the drain flag properly" in new TestWithSourceProbeAndSinkProbe {
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
