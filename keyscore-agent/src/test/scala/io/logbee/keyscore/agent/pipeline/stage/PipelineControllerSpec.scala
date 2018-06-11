package io.logbee.keyscore.agent.pipeline.stage

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import io.logbee.keyscore.agent.pipeline.ExampleData._
import io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic
import io.logbee.keyscore.agent.pipeline.{Controller, TestSystemWithMaterializerAndExecutionContext}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextMapParameter}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class PipelineControllerSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext  {
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
    "should let data pass as expected" in new TestSetup {
      source.sendNext(dataset1)
      sink.request(1)
      sink.expectNext(dataset1)
    }

    "a dataset inserted in inValve should be extracted in outValve" in new TestSetup {
      whenReady(controllerFuture) { controller =>
        whenReady(controller.insert(dataset1)) { state =>
//          whenReady(controller.extractInsertedData()) {dataset =>
//            dataset should have size 1
//          }
        }

      }


    }
  }

}
