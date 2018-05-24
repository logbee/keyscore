package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.actor.ActorRef
import akka.stream.{SinkShape, SourceShape}
import akka.testkit.{TestActor, TestProbe}
import io.logbee.keyscore.agent.stream.FilterManager.{CreateSinkStage, CreateSourceStage}
import io.logbee.keyscore.agent.stream.StreamSupervisor.CreateStream
import io.logbee.keyscore.agent.stream.stage._
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import io.logbee.keyscore.model.{Dataset, StreamConfiguration}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class StreamSupervisorSpec extends WordSpec with Matchers with ScalaFutures with  MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A running StreamSupervisor" should {

    val filterManager = TestProbe()

    val streamId = UUID.randomUUID()
    val sourceConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-source"))
    val sinkConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-sink"))
    val streamConfiguration = StreamConfiguration(streamId, "test", "A test stream.", sourceConfiguration, List.empty, sinkConfiguration)

    val supervisor = system.actorOf(StreamSupervisor(filterManager.ref))

    "fubar" in {

      val sinkStage = new SinkStage(stub[StageContext], sinkConfiguration, sinkLogicProvider)
      val sourceStage = new SourceStage(stub[StageContext], sourceConfiguration, sourceLogicProvider)

      filterManager.setAutoPilot((sender: ActorRef, message: Any) => message match {
        case _: CreateSinkStage =>
          sender ! FilterManager.SinkStageCreated(sinkStage)
          TestActor.KeepRunning
        case _: CreateSourceStage =>
          sender ! FilterManager.SourceStageCreated(sourceStage)
          TestActor.KeepRunning
      })

      supervisor ! CreateStream(streamConfiguration)

      Thread.sleep(30000)
    }
  }

  val sourceLogicProvider = (ctx: StageContext, config: FilterConfiguration, shape: SourceShape[Dataset]) => {
    new SourceLogic(ctx, config, shape) {
      override def configure(configuration: FilterConfiguration): Unit = ???
      override def onPull(): Unit = ???
    }
  }

  val sinkLogicProvider = (ctx: StageContext, config: FilterConfiguration, shape: SinkShape[Dataset]) => {
    new SinkLogic(ctx, config, shape) {
      override def configure(configuration: FilterConfiguration): Unit = ???
      override def onPush(): Unit = ???
    }
  }
}
