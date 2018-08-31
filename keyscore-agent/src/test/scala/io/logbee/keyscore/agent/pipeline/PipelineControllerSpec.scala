package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.stream.FlowShape
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, LogicParameters, StageContext}
import io.logbee.keyscore.agent.pipeline.valve.ValveStage
import io.logbee.keyscore.commons.test.TestSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.model.configuration.{Configuration, FieldListParameter}
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.{After, Before}
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
    val configuration = Configuration(parameters = Seq(
      FieldListParameter(AddFieldsFilterLogic.fieldListParameter.ref, Seq()
      )))
    val testSource = TestSource.probe[Dataset]
    val testsink = TestSink.probe[Dataset]
    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: Configuration, s: FlowShape[Dataset, Dataset]) =>
      new AddFieldsFilterLogic(LogicParameters(UUID.randomUUID(),ctx, c), s))

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
        .toMat(testsink)(Keep.both).run()
  }

  "A Pipeline" should {

    "let data pass as expected" in new TestSetup {
      source.sendNext(dataset1)
      source.sendNext(dataset2)
      source.sendNext(dataset3)
      sink.requestNext().records should contain theSameElementsAs dataset1.records
      sink.requestNext().records should contain theSameElementsAs dataset2.records
      sink.requestNext().records should contain theSameElementsAs dataset3.records
    }

    //    TODO: Fix flaky ValveStage throughputTime computation test
    //    "valve computes and sets the throughputTime and totalThroughputTime in valvestate" in new TestSetup {
    //      whenReady(controllerFuture) { controller =>
    //
    //        source.sendNext(dataset1)
    //        sink.requestNext().records should contain theSameElementsAs dataset1.records
    //
    //        whenReady(controller.state()) { state =>
    //          state.throughPutTime.toInt should be > 0
    //          state.totalThroughputTime.toInt should be > 0
    //          state.health shouldBe Green
    //          state.status shouldBe Running
    //        }
    //      }
    //    }

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

    "extract a dataset in outValve when no data was streamed before" in new TestSetup {
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

    "extract a dataset in outValve when data was streamed before" in new TestSetup {
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

    "extract multiple datasets in outValve" in new TestSetup {
      whenReady(controllerFuture) { controller =>

        val state = for {
          _ <- controller.pause(true)
          _ <- controller.drain(true)
          filterState <- controller.insert(List(dataset1, dataset2, dataset3), Before)
        } yield filterState

        whenReady(state) { _ =>

          whenReady(controller.extract(3, After)) { datasets =>

            datasets(0).records should contain theSameElementsAs dataset3.records
            datasets(1).records should contain theSameElementsAs dataset2.records
            datasets(2).records should contain theSameElementsAs dataset1.records

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

    "extract insert workflow test real" in new TestSetup {

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
            datasets(0).records should contain theSameElementsAs dataset4.records
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
            datasets(0).records should contain theSameElementsAs dataset5.records
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
