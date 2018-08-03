package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{FlowShape, SinkShape, SourceShape}
import akka.testkit.{TestActor, TestKit, TestProbe}
import io.logbee.keyscore.agent.pipeline.FilterManager.{CreateFilterStage, CreateSinkStage, CreateSourceStage}
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor.CreatePipeline
import io.logbee.keyscore.agent.pipeline.stage._
import io.logbee.keyscore.commons.pipeline.RequestPipelineInstance
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class PipelineSupervisorSpec extends TestKit(ActorSystem("actorSystem")) with WordSpecLike with Matchers with ScalaFutures with MockFactory  {

  "A running PipelineSupervisor" should {

    val filterManager = TestProbe()
    val pipelineID = UUID.randomUUID()
    val sourceConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-source"))
    val sinkConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-sink"))
    val filterConfiguration1 = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-filter1"))
    val filterConfiguration2 = FilterConfiguration(FilterDescriptor(UUID.randomUUID(),"test-filter2"))

    val pipelineConfiguration = PipelineConfiguration(pipelineID, "test", "A test pipeline.", sourceConfiguration, List(filterConfiguration1,filterConfiguration2), sinkConfiguration)
    val agent = TestProbe("agent")
    val supervisor = system.actorOf(PipelineSupervisor(filterManager.ref))

    trait SupervisorSpecSetup {
      val sinkStage = new SinkStage(stub[StageContext], sinkConfiguration, sinkLogicProvider)
      val sourceStage = new SourceStage(stub[StageContext], sourceConfiguration, sourceLogicProvider)
      val filterStage = new FilterStage(stub[StageContext], filterConfiguration1, filterLogicProvider)
      val filterStage2 = new FilterStage(stub[StageContext],filterConfiguration2,filterLogicProvider)

      filterManager.setAutoPilot((sender: ActorRef, message: Any) => message match {
        case _: CreateSinkStage =>
          sender ! FilterManager.SinkStageCreated(sinkStage)
          TestActor.KeepRunning
        case _: CreateSourceStage =>
          sender ! FilterManager.SourceStageCreated(sourceStage)
          TestActor.KeepRunning
        case _: CreateFilterStage =>
          sender ! FilterManager.FilterStageCreated(filterStage)
          sender ! FilterManager.FilterStageCreated(filterStage2)
          TestActor.KeepRunning
      })
    }
    def pollPipelineHealthState(maxRetries: Int, sleepTimeMs: Long): Boolean = {
      var retries = maxRetries
      while (retries > 0) {

        supervisor tell(RequestPipelineInstance(agent.ref), agent.ref)
        val pipelineInstance = agent.receiveOne(2 seconds).asInstanceOf[PipelineInstance]
        if (pipelineInstance.health.equals(Green)) {
          return true
        }
        Thread.sleep(sleepTimeMs)
        retries -= 1
      }

      false
    }

    "start a pipeline with a correct configuration" in new SupervisorSpecSetup {

      supervisor tell (RequestPipelineInstance(agent.ref),agent.ref)
      agent.expectMsg(PipelineInstance(UUID.fromString("00000000-0000-0000-0000-000000000000"),"", "", null, Red))

      supervisor ! CreatePipeline(pipelineConfiguration)

      supervisor tell (RequestPipelineInstance(agent.ref), agent.ref)
      agent.expectMsg(PipelineInstance(pipelineConfiguration.id,pipelineConfiguration.name, pipelineConfiguration.description, pipelineConfiguration.id, Red))

      pollPipelineHealthState(maxRetries = 10, sleepTimeMs = 2000) shouldBe true

    }
  }

  val sourceLogicProvider = (ctx: StageContext, config: FilterConfiguration, shape: SourceShape[Dataset]) => {
    new SourceLogic(ctx, config, shape) {
      val input: List[String] = List("first","second","third")
      var outputCounter = 0

      override def configure(configuration: FilterConfiguration): Unit = ???

      override def onPull(): Unit = {
        if (outputCounter < input.size) {
          push(shape.out, Dataset(Record(TextField(outputCounter.toString, input(outputCounter)))))
          outputCounter += 1
        }
      }
    }
  }

  val sinkLogicProvider = (ctx: StageContext, config: FilterConfiguration, shape: SinkShape[Dataset]) => {
    new SinkLogic(ctx, config, shape) {
      override def initialize(configuration: FilterConfiguration): Unit = {
        //configure(configuration)
        pull(in)
      }

      override def configure(configuration: FilterConfiguration): Unit = ???

      override def onPush(): Unit ={
        val result = grab(in)
        assert(result.records.head.fields.head.value.asInstanceOf[TextValue].value.contains("_modified"))
        println(result)
        pull(in)
      }
    }
  }

  val filterLogicProvider = (ctx: StageContext, config: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) => {
    new FilterLogic(ctx, config, shape) {

      override def configure(configuration: FilterConfiguration): Unit = ???

      override def onPush(): Unit = {
        val dataset = grab(in)
        println(dataset)
        val modifiedValue = dataset.records.head.fields.head.value + "_modified"
        val newRecord = Record(TextField(dataset.records.head.fields.head.name, modifiedValue))

        push(out, Dataset(newRecord))
      }

      override def onPull(): Unit = {
        pull(in)
      }
    }
  }
}
