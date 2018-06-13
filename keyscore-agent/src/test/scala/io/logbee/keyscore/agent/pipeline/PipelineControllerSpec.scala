package io.logbee.keyscore.agent.pipeline

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic
import io.logbee.keyscore.agent.pipeline.stage.{FilterStage, StageContext, ValveStage}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextMapParameter}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps


//@RunWith(classOf[JUnitRunner])
class PipelineControllerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  trait TestSetup {
    val configuration = FilterConfiguration(randomUUID(), FilterDescriptor(randomUUID(), "test"), List(TextMapParameter("fieldsToAdd", Map.empty)))
    val testSource = TestSource.probe[Dataset]
    val testsink = TestSink.probe[Dataset]
    val context = StageContext(system, executionContext)
    val filterStage = new FilterStage(context, configuration, (ctx: StageContext, c: FilterConfiguration, s: FlowShape[Dataset, Dataset]) => new AddFieldsFilterLogic(ctx, c, s))

    val ((source, controllerFuture), sink) =
      testSource.viaMat(new ValveStage())(Keep.both)
        .viaMat(filterStage)(Keep.both)
        .viaMat(new ValveStage()) { (left, right) =>
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
      sink.request(1)
      sink.expectNext(dataset1)
    }

    "close inValve and outValve on pause" in new TestSetup {
      source.sendNext(dataset1)
      source.sendNext(dataset2)
      source.sendNext(dataset3)

      whenReady(controllerFuture) { controller =>
        Await.ready(controller.pause(), 5 seconds)
        sink.request(1)
        sink.expectNoMessage(5 seconds)

        whenReady(controller.unpause()) { _ =>
          sink.request(1)
          sink.expectNext(dataset1)
        }
      }
    }

    //TODO:adjust ValveStage implementation
//    "extract a dataset in outValve" in new TestSetup {
//      whenReady(controllerFuture) {controller =>
//        source.sendNext(dataset1)
//        source.sendNext(dataset1)
//        source.sendNext(dataset1)
//        source.sendNext(dataset1)
//        sink.request(4)
//
//        whenReady(controller.insert(dataset1)) { _ =>
//        }
//        whenReady(controller.extract()) { datasets =>
//          datasets should have size 1
//        }
//      }
//    }

    "drain valve" in new TestSetup {
      whenReady(controllerFuture) { controller =>
        source.sendNext(dataset1)
        source.sendNext(dataset1)
        sink.request(1)
        whenReady(controller.pause()) { _ =>

        }
        whenReady(controller.drain(true)) { _ =>

        }
      }
    }
  }
}
