package io.logbee.keyscore.agent.pipeline

import java.util.UUID

import akka.actor.ActorRef
import akka.stream.{FlowShape, SinkShape, SourceShape}
import akka.testkit.{TestActor, TestProbe}
import io.logbee.keyscore.agent.pipeline.FilterManager.{CreateFilterStage, CreateSinkStage, CreateSourceStage}
import io.logbee.keyscore.agent.pipeline.PipelineSupervisor.CreatePipeline
import io.logbee.keyscore.agent.pipeline.stage._
import io.logbee.keyscore.commons.cluster.resources.ConfigurationMessages.StoreConfigurationRequest
import io.logbee.keyscore.commons.cluster.resources.DescriptorMessages.StoreDescriptorRequest
import io.logbee.keyscore.commons.pipeline.RequestPipelineInstance
import io.logbee.keyscore.commons.test.ProductionSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.frontier.cluster.resources.{ConfigurationManager, DescriptorManager}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.configuration.{Configuration, ConfigurationRef}
import io.logbee.keyscore.model.conversion.UUIDConversion.uuidFromString
import io.logbee.keyscore.model.data.{Dataset, Record, TextField, TextValue}
import io.logbee.keyscore.model.descriptor.{Descriptor, DescriptorRef}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class PipelineSupervisorSpec extends ProductionSystemWithMaterializerAndExecutionContext with WordSpecLike with Matchers with ScalaFutures with MockFactory {

  "A running PipelineSupervisor" should {

    val filterManager = TestProbe()

    val sourceConfigurationRef = ConfigurationRef("bae4e0bc-2784-416a-a93d-0e36ed80d6e0")
    val sourceDescriptorRef = DescriptorRef("15072c62-b4c0-47d5-b842-0256dc066bb9")

    val filterConfigurationRef = ConfigurationRef("d2588462-b5f4-4b10-8cbb-7bcceb178cb5")
    val filterDescriptorRef = DescriptorRef("b3482a2d-2df9-4d77-8c03-36728b29da8e")

    val sinkConfigurationRef = ConfigurationRef("05dc6d8a-50ff-41bd-b637-5132be1f2415")
    val sinkDescriptorRef = DescriptorRef("10afeaa0-6395-464b-935d-227b160c1412")

    val descriptorManager = system.actorOf(DescriptorManager())
    val configurationManager = system.actorOf(ConfigurationManager())

    configurationManager ! StoreConfigurationRequest(Configuration(sourceConfigurationRef))
    configurationManager ! StoreConfigurationRequest(Configuration(filterConfigurationRef))
    configurationManager ! StoreConfigurationRequest(Configuration(sinkConfigurationRef))

    descriptorManager ! StoreDescriptorRequest(Descriptor(sourceDescriptorRef))
    descriptorManager ! StoreDescriptorRequest(Descriptor(filterDescriptorRef))
    descriptorManager ! StoreDescriptorRequest(Descriptor(sinkDescriptorRef))

    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("10d3e280-cb7c-4a77-be1f-8ccf5f1b0df2"), Seq(
      SourceBlueprint(BlueprintRef("d69c8aca-2ceb-49c5-b4f8-f8298e5187cd"), sourceDescriptorRef, sourceConfigurationRef),
      FilterBlueprint(BlueprintRef("24a88215-cfe0-47a1-a889-7f3e9f8260ef"), filterDescriptorRef, filterConfigurationRef),
      SinkBlueprint(BlueprintRef("80851e06-7191-4d96-8e4d-de66a5a12e81"), sinkDescriptorRef, sinkConfigurationRef)),
      name = "exampeBlueprint",
      description = "Hello World!"
    )

    val agent = TestProbe("agent")
    val supervisor = system.actorOf(PipelineSupervisor(filterManager.ref))

    trait SupervisorSpecSetup {
      val sinkStage = new SinkStage(LogicParameters(UUID.randomUUID(), stub[StageContext], Configuration()), sinkLogicProvider)
      val sourceStage = new SourceStage(LogicParameters(UUID.randomUUID(), stub[StageContext], Configuration()), sourceLogicProvider)
      val filterStage = new FilterStage(LogicParameters(UUID.randomUUID(), stub[StageContext], Configuration()), filterLogicProvider)
      val filterStage2 = new FilterStage(LogicParameters(UUID.randomUUID(), stub[StageContext], Configuration()), filterLogicProvider)

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

      supervisor ! CreatePipeline(pipelineBlueprint)

      supervisor tell(RequestPipelineInstance(agent.ref), agent.ref)
      agent.expectMsg(PipelineInstance(pipelineBlueprint.ref.uuid, pipelineBlueprint.name, pipelineBlueprint.description, Red))

      pollPipelineHealthState(maxRetries = 10, sleepTimeMs = 2000) shouldBe true

    }
  }

  val sourceLogicProvider = (parameters: LogicParameters, shape: SourceShape[Dataset]) => {
    new SourceLogic(parameters, shape) {
      val input: List[String] = List("first", "second", "third")
      var outputCounter = 0

      override def initialize(configuration: Configuration): Unit = {}

      override def configure(configuration: Configuration): Unit = {}

      override def onPull(): Unit = {
        if (outputCounter < input.size) {
          push(shape.out, Dataset(Record(TextField(outputCounter.toString, input(outputCounter)))))
          outputCounter += 1
        }
      }

    }
  }

  val sinkLogicProvider = (parameters: LogicParameters, shape: SinkShape[Dataset]) => {
    new SinkLogic(parameters, shape) {
      override def initialize(configuration: Configuration): Unit = {
        pull(in)
      }

      override def configure(configuration: Configuration): Unit = {}

      override def onPush(): Unit = {
        val result = grab(in)
        assert(result.records.head.fields.head.value.asInstanceOf[TextValue].value.contains("_modified"))
        println(result)
        pull(in)
      }
    }
  }

  val filterLogicProvider = (parameters: LogicParameters, shape: FlowShape[Dataset, Dataset]) => {
    new FilterLogic(parameters, shape) {

      override def initialize(configuration: Configuration): Unit = {}

      override def configure(configuration: Configuration): Unit = {}

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
