package io.logbee.keyscore.agent.pipeline

import akka.testkit.TestProbe
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.FilterExtension
import io.logbee.keyscore.commons.test.TestSystemWithMaterializerAndExecutionContext
import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class FilterManagerSpec extends FreeSpec with TestSystemWithMaterializerAndExecutionContext {

  "A FilterManager" - {

    val filterManager = system.actorOf(FilterManager.props())

    "should gather all registered filters" in {

      val probe = TestProbe()

      filterManager ! RegisterExtension(FilterExtension, classOf[ExampleFilter])

      filterManager tell(FilterManager.RequestDescriptors, probe.ref)

      val response = probe.receiveOne(10 seconds).asInstanceOf[FilterManager.DescriptorsResponse]


    }

    "send a stage" in {

      //      val stageContext = StageContext(system, executionContext)
      //      val configuration = FilterConfiguration(FilterDescriptor(randomUUID(), "io.logbee.keyscore.agent.pipeline.DummyFilterLogic"))
      //
      //      val probe = TestProbe()
      //      filterManager tell (FilterManager.CreateFilterStage(stageContext, configuration), probe.ref)
      //
      //      val message = probe.receiveOne(10 seconds).asInstanceOf[FilterManager.FilterStageCreated]
    }
  }
}

//class DummyFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {
//
//  override def configure(configuration: FilterConfiguration): Unit = {}
//
//  override def onPush(): Unit = {}
//
//  override def onPull(): Unit = {}
//}

