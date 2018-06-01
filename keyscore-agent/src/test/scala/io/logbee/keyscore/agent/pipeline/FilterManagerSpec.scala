package io.logbee.keyscore.agent.pipeline

import java.util.UUID.randomUUID

import akka.stream.FlowShape
import akka.testkit.TestProbe
import io.logbee.keyscore.agent.pipeline.stage.{FilterLogic, StageContext}
import io.logbee.keyscore.model.Dataset
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import org.junit.runner.RunWith
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration._
import scala.language.postfixOps


@RunWith(classOf[JUnitRunner])
class FilterManagerSpec extends WordSpecLike with TestSystemWithMaterializerAndExecutionContext {

  "A FilterManager" should {

    val filterManager = system.actorOf(FilterManager.props())

    "send a stage" in {

      val stageContext = StageContext(system, executionContext)
      val configuration = FilterConfiguration(FilterDescriptor(randomUUID(), "io.logbee.keyscore.agent.pipeline.DummyFilterLogic"))

      val probe = TestProbe()
      filterManager tell (FilterManager.CreateFilterStage(stageContext, configuration), probe.ref)

      val message = probe.receiveOne(10 seconds).asInstanceOf[FilterManager.FilterStageCreated]
    }
  }
}

class DummyFilterLogic(context: StageContext, configuration: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) extends FilterLogic(context, configuration, shape) {

  override def configure(configuration: FilterConfiguration): Unit = {}

  override def onPush(): Unit = {}

  override def onPull(): Unit = {}
}