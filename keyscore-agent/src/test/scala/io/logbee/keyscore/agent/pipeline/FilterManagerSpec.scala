package io.logbee.keyscore.agent.pipeline

import akka.stream.FlowShape
import akka.testkit.TestProbe
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.FilterExtension
import io.logbee.keyscore.model.Described
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.Dataset
import io.logbee.keyscore.model.descriptor._
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

object ExampleFilter extends Described {

  private val filterId = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610"

  override def describe: Descriptor = {
    Descriptor(filterId,
      describes = FilterDescriptor(
        name = classOf[ExampleFilter].getName
      )
    )
  }
}
class ExampleFilter(context: StageContext, configuration: Configuration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  override def initialize(configuration: Configuration): Unit = configure(configuration)

  override def configure(configuration: Configuration): Unit = {}

  override def onPush(): Unit = {}

  override def onPull(): Unit = {}
}