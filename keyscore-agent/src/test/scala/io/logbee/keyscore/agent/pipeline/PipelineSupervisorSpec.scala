package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{FlowShape, SinkShape, SourceShape}
import akka.testkit.{TestActor, TestKit, TestProbe}
import io.logbee.keyscore.agent.pipeline.FilterManager.{CreateFilterStage, CreateSinkStage, CreateSourceStage}
import io.logbee.keyscore.agent.pipeline.stage._
import io.logbee.keyscore.commons.pipeline.RequestPipelineInstance
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.configuration.Configuration
import io.logbee.keyscore.model.data.{Dataset, Record, TextField, TextValue}
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
//    val sourceConfiguration = Configuration(FilterDescriptor(UUID.randomUUID(), "test-source"))
//    val sinkConfiguration = Configuration(FilterDescriptor(UUID.randomUUID(), "test-sink"))
//    val filterConfiguration1 = Configuration(FilterDescriptor(UUID.randomUUID(), "test-filter1"))
//    val filterConfiguration2 = Configuration(FilterDescriptor(UUID.randomUUID(),"test-filter2"))

//    val pipelineConfiguration = PipelineConfiguration(pipelineID, "test", "A test pipeline.", Configuration(), List(filterConfiguration1,filterConfiguration2), sinkConfiguration)
    val agent = TestProbe("agent")
    val supervisor = system.actorOf(PipelineSupervisor(filterManager.ref))

    trait SupervisorSpecSetup {
      val sinkStage = new SinkStage(stub[StageContext], Configuration(), sinkLogicProvider)
      val sourceStage = new SourceStage(stub[StageContext], Configuration(), sourceLogicProvider)
      val filterStage = new FilterStage(stub[StageContext], Configuration(), filterLogicProvider)
      val filterStage2 = new FilterStage(stub[StageContext],Configuration(),filterLogicProvider)

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

//      supervisor ! CreatePipeline(pipelineConfiguration)

//      supervisor tell (RequestPipelineInstance(agent.ref), agent.ref)
//      agent.expectMsg(PipelineInstance(pipelineConfiguration.id,pipelineConfiguration.name, pipelineConfiguration.description, pipelineConfiguration.id, Red))
//
//      pollPipelineHealthState(maxRetries = 10, sleepTimeMs = 2000) shouldBe true

    }
  }

  val sourceLogicProvider = (ctx: StageContext, config: Configuration, shape: SourceShape[Dataset]) => {
    new SourceLogic(LogicParameters(null, ctx, config), shape) {
      val input: List[String] = List("first","second","third")
      var outputCounter = 0

      override def configure(configuration: Configuration): Unit = ???

      override def onPull(): Unit = {
        if (outputCounter < input.size) {
          push(shape.out, Dataset(Record(TextField(outputCounter.toString, input(outputCounter)))))
          outputCounter += 1
        }
      }
    }
  }

  val sinkLogicProvider = (ctx: StageContext, config: Configuration, shape: SinkShape[Dataset]) => {
    new SinkLogic(LogicParameters(null, ctx, config), shape) {
      override def initialize(configuration: Configuration): Unit = {
        //configure(configuration)
        pull(in)
      }

      override def configure(configuration: Configuration): Unit = ???

      override def onPush(): Unit ={
        val result = grab(in)
        assert(result.records.head.fields.head.value.asInstanceOf[TextValue].value.contains("_modified"))
        println(result)
        pull(in)
      }
    }
  }

  val filterLogicProvider = (ctx: StageContext, config: Configuration, shape: FlowShape[Dataset, Dataset]) => {
    new FilterLogic(LogicParameters(null, ctx, config), shape) {

      override def configure(configuration: Configuration): Unit = ???

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
