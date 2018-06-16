package io.logbee.keyscore.agent.pipeline

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext}
import io.logbee.keyscore.agent.pipeline.valve.ValveStage
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextMapParameter}
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
    val configuration = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(TextMapParameter("fieldsToAdd", Map.empty)))
    val testSource = TestSource.probe[Dataset]
    val testsink = TestSink.probe[Dataset]
    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new AddFieldsFilterLogic(ctx, c, s))

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
      sink.request(3)
      sink.expectNext(dataset1)
      sink.expectNext(dataset2)
      sink.expectNext(dataset3)
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
          sink.expectNext(dataset1)
        }
      }
    }

    "extract a dataset in outValve when no data was streamed before" in new TestSetup {
      whenReady(controllerFuture) { controller =>
        whenReady(controller.insert(dataset1)) { _ =>
          whenReady(controller.extract()) { datasets =>
            datasets should contain(dataset1)
          }
        }
      }
    }

    "extract a dataset in outValve when data was streamed before" in new TestSetup {
      source.sendNext(dataset1)
      source.sendNext(dataset2)
      sink.request(2)
      sink.expectNext(dataset1)
      sink.expectNext(dataset2)

      whenReady(controllerFuture) { controller =>
        whenReady(controller.insert(dataset3)) { _ =>
          whenReady(controller.extract()) { datasets =>
            datasets should contain(dataset3)
          }
        }
      }
    }

    "extract multiple datasets in outValve" in new TestSetup {
      whenReady(controllerFuture) { controller =>
        whenReady(controller.insert(dataset1, dataset2, dataset3)) { _ =>
          whenReady(controller.extract(3)) { datasets =>
            datasets should contain inOrderOnly(dataset3, dataset2, dataset1)
          }
        }

        whenReady(controller.drain(false)) { _ =>
          whenReady(controller.pause(false)) { _ =>
            source.sendNext(dataset4)
            sink.request(1)
            sink.expectNext(dataset4)
          }
        }
      }
    }

    "extract insert workflow test real" in new TestSetup {
      source.sendNext(dataset1)
      source.sendNext(dataset1)
      source.sendNext(dataset1)
      sink.request(3)
      sink.expectNext(dataset1)
      sink.expectNext(dataset1)
      sink.expectNext(dataset1)

      whenReady(controllerFuture) { controller =>
        whenReady(controller.insert(dataset2, dataset3, dataset4)) { _ =>
          whenReady(controller.extract(3)) { datasets =>
            datasets should contain inOrderOnly(dataset4, dataset3, dataset2)
          }
        }

        whenReady(controller.drain(false)) { _ =>
          whenReady(controller.pause(false)) { _ =>
            source.sendNext(dataset4)
            sink.request(1)
            sink.expectNext(dataset4)

          }
        }
        whenReady(controllerFuture) { controller =>
          whenReady(controller.insert(dataset2, dataset3, dataset5)) { _ =>
            whenReady(controller.extract(3)) { datasets =>
              datasets should contain inOrderOnly(dataset5, dataset3, dataset2)
            }
          }

          whenReady(controller.drain(false)) { _ =>
            whenReady(controller.pause(false)) { _ =>
              source.sendNext(dataset1)
              sink.request(1)
              sink.expectNext(dataset1)

            }
          }
        }
      }
    }
  }
}
