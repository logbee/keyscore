package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.controller.Controller
import io.logbee.keyscore.agent.pipeline.valve.ValveStage
import io.logbee.keyscore.agent.pipeline.valve.ValveStage._totalThroughputTime
import io.logbee.keyscore.model.configuration.{Configuration, FieldListParameter, ParameterSet}
import io.logbee.keyscore.model.data.{Dataset, Label, Record, TextValue}
import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.metrics.{CounterMetric, GaugeMetric}
import io.logbee.keyscore.model.pipeline.Running
import io.logbee.keyscore.model.{After, Before}
import io.logbee.keyscore.pipeline.api.LogicParameters
import io.logbee.keyscore.pipeline.api.stage.{FilterStage, StageContext}
import io.logbee.keyscore.pipeline.contrib.filter.AddFieldsLogic
import io.logbee.keyscore.test.fixtures.ExampleData._
import io.logbee.keyscore.test.fixtures.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.time.{Second, Seconds, Span}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class PipelineControllerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  override implicit val patienceConfig = PatienceConfig(Span(10, Seconds), Span(1, Second))

  trait TestSetup {
    val configuration = Configuration(parameterSet = ParameterSet(Seq(
      FieldListParameter(AddFieldsLogic.fieldListParameter.ref, Seq())
    )))
    val testSource = TestSource.probe[Dataset]
    val testSink = TestSink.probe[Dataset]
    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), context, configuration), (p: LogicParameters, s: FlowShape[Dataset, Dataset]) => new ExampleFilter(p, s))

    val inLabel = Label("port", TextValue("in"))
    val outLabel = Label("port", TextValue("out"))

    val ((source, controllerFuture), sink) =
      testSource.viaMat(new ValveStage())(Keep.both)
        .viaMat(filterStage)(Keep.both)
        .viaMat(new ValveStage(5)) { (left, right) =>
          left match {
            case ((source, inValveProxyFuture), filterProxyFuture) =>
              val controller = for {
                inValveProxy <- inValveProxyFuture
                filterProxy <- filterProxyFuture
                outValveProxy <- right
              } yield Controller.filterController(inValveProxy, filterProxy, outValveProxy)

              (source, controller)
          }
        }
        .toMat(testSink)(Keep.both).run()
  }

  "A PipelineController" should {

    "return the correct total_throughputTime" in new TestSetup {
      whenReady(controllerFuture) { controller =>

        Thread.sleep(5000)
        source.sendNext(Dataset(Record()))
        sink.requestNext()

        whenReady(controller.state()) { state =>

          whenReady(controller.scrape()) { collection =>
            val metrics = collection.findMetrics[GaugeMetric]("total_throughput_time")
            val times = metrics.map(_.value)
            times should contain(state.totalThroughputTime)
          }
        }
      }
    }

    "not affect the data in a stream" in new TestSetup {
      source.sendNext(dataset1)
      source.sendNext(dataset2)
      source.sendNext(dataset3)
      sink.requestNext().records should contain theSameElementsAs dataset1.records
      sink.requestNext().records should contain theSameElementsAs dataset2.records
      sink.requestNext().records should contain theSameElementsAs dataset3.records
    }

    "retrieve the state of its pipeline" in new TestSetup {
      whenReady(controllerFuture) { controller =>

        source.sendNext(dataset1)
        sink.requestNext().records should contain theSameElementsAs dataset1.records

        whenReady(controller.state()) { state =>
          state.health shouldBe Green
          state.status shouldBe Running
        }

        whenReady(controller.scrape()) { collection =>
          collection.metrics shouldNot be(empty)
          val in = collection.findMetrics[GaugeMetric]("throughput_time", Set(inLabel))
          in.size should be(1)
          val out = collection.findMetrics[CounterMetric]("pushed_datasets", Set(outLabel))
          out.size should be(1)
          out.head.value should be(1.0)
        }
      }
    }

    "close inValve and outValve on pause" in new TestSetup {
      source.sendNext(dataset1)
      source.sendNext(dataset2)
      source.sendNext(dataset3)

      whenReady(controllerFuture) { controller =>
        Await.ready(controller.pause(true), 5 seconds)
        sink.request(1)
        sink.expectNoMessage(5 seconds)

        whenReady(controller.pause(false)) { _ =>
          sink.request(1)
          sink.requestNext().records should contain theSameElementsAs dataset1.records
        }
      }
    }

    "extract the inserted dataset in the outValve when no data was streamed before" in new TestSetup {
      whenReady(controllerFuture) { controller =>
        val state = for {
          _ <- controller.pause(true)
          _ <- controller.drain(true)
          insert <- controller.insert(List(dataset1), Before)
        } yield insert

        whenReady(state) { _ =>
          whenReady(controller.extract(where = After)) { datasets =>
            datasets.head.records should contain theSameElementsAs dataset1.records
          }
        }
      }
    }

    "extract the inserted dataset in the outValve when data was streamed before" in new TestSetup {
      source.sendNext(dataset1)
      source.sendNext(dataset2)
      sink.request(2)
      sink.requestNext().records should contain theSameElementsAs dataset1.records
      sink.requestNext().records should contain theSameElementsAs dataset2.records

      whenReady(controllerFuture) { controller =>
        whenReady(controller.insert(List(dataset3), Before)) { _ =>
          whenReady(controller.extract(where = After)) { datasets =>
            datasets.head.records should contain theSameElementsAs dataset3.records
          }
        }
      }
    }

    "extract multiple datasets in the outValve" in new TestSetup {
      whenReady(controllerFuture) { controller =>

        val state = for {
          _ <- controller.pause(true)
          _ <- controller.drain(true)
          filterState <- controller.insert(List(dataset1, dataset2, dataset3), Before)
        } yield filterState

        whenReady(state) { _ =>

          whenReady(controller.extract(3, After)) { datasets =>

            datasets.head.records should contain theSameElementsAs dataset3.records
            datasets(1).records should contain theSameElementsAs dataset2.records
            datasets(2).records should contain theSameElementsAs dataset1.records

            whenReady(controller.scrape()) { collection =>
              val extracted = collection.findMetrics[CounterMetric]("extracted_datasets", Set(outLabel))
              extracted.size should be(1)
              extracted.head.value should be(3.0)
            }

            whenReady(controller.drain(false)) { _ =>
              whenReady(controller.pause(false)) { _ =>
                source.sendNext(dataset4)
                sink.request(1)
                sink.requestNext().records should contain theSameElementsAs dataset4.records
              }
            }
          }
        }
      }
    }

    "not affect the data in a stream after multiple valve actions" in new TestSetup {

      source.sendNext(dataset1)
      source.sendNext(dataset2)
      source.sendNext(dataset3)

      sink.requestNext().records should contain theSameElementsAs dataset1.records
      sink.requestNext().records should contain theSameElementsAs dataset2.records
      sink.requestNext().records should contain theSameElementsAs dataset3.records

      whenReady(controllerFuture) { controller =>

        var state = for {
          _ <- controller.pause(true)
          _ <- controller.drain(true)
          filterState <- controller.insert(List(dataset2, dataset3, dataset4), Before)
        } yield filterState

        whenReady(state) { _ =>
          whenReady(controller.extract(3, After)) { datasets =>
            datasets.head.records should contain theSameElementsAs dataset4.records
            datasets(1).records should contain theSameElementsAs dataset3.records
            datasets(2).records should contain theSameElementsAs dataset2.records
          }
        }

        whenReady(controller.drain(false)) { _ =>
          whenReady(controller.pause(false)) { _ =>
            source.sendNext(dataset1)
            sink.requestNext().records should contain theSameElementsAs dataset1.records
          }
        }

        state = for {
          _ <- controller.pause(true)
          _ <- controller.drain(true)
          filterState <- controller.insert(List(dataset2, dataset3, dataset5), Before)
        } yield filterState

        whenReady(state) { _ =>
          whenReady(controller.extract(3, After)) { datasets =>
            datasets.head.records should contain theSameElementsAs dataset5.records
            datasets(1).records should contain theSameElementsAs dataset3.records
            datasets(2).records should contain theSameElementsAs dataset2.records
          }
        }

        whenReady(controller.drain(false)) { _ =>
          whenReady(controller.pause(false)) { _ =>
            source.sendNext(dataset1)
            sink.requestNext().records should contain theSameElementsAs dataset1.records
          }
        }
      }
    }
  }
}
