package io.logbee.keyscore.agent.stream

import java.util.UUID

import akka.actor.ActorRef
import akka.stream.{FlowShape, SinkShape, SourceShape}
import akka.testkit.{TestActor, TestProbe}
import io.logbee.keyscore.agent.stream.FilterManager.{CreateFilterStage, CreateSinkStage, CreateSourceStage}
import io.logbee.keyscore.agent.stream.StreamSupervisor.CreateStream
import io.logbee.keyscore.agent.stream.stage._
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor}
import io.logbee.keyscore.model.{Dataset, Record, StreamConfiguration, TextField}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ListBuffer


@RunWith(classOf[JUnitRunner])
class StreamSupervisorSpec extends WordSpec with Matchers with ScalaFutures with MockFactory with TestSystemWithMaterializerAndExecutionContext {

  "A running StreamSupervisor" should {

    val filterManager = TestProbe()

    val streamId = UUID.randomUUID()
    val sourceConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-source"))
    val sinkConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-sink"))
    val filterConfiguration = FilterConfiguration(FilterDescriptor(UUID.randomUUID(), "test-filter1"))
    val filterConfiguration2 = FilterConfiguration(FilterDescriptor(UUID.randomUUID(),"test-filter2"))

    val streamConfiguration = StreamConfiguration(streamId, "test", "A test stream.", sourceConfiguration, List(filterConfiguration,filterConfiguration2), sinkConfiguration)

    val supervisor = system.actorOf(StreamSupervisor(filterManager.ref))

    "fubar" in {

      val sinkStage = new SinkStage(stub[StageContext], sinkConfiguration, sinkLogicProvider)
      val sourceStage = new SourceStage(stub[StageContext], sourceConfiguration, sourceLogicProvider)
      val filterStage = new FilterStage(stub[StageContext], filterConfiguration, filterLogicProvider)
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

      supervisor ! CreateStream(streamConfiguration)

      Thread.sleep(30000)
    }
  }

  val sourceLogicProvider = (ctx: StageContext, config: FilterConfiguration, shape: SourceShape[Dataset]) => {
    new SourceLogic(ctx, config, shape) {
      val input: List[String] = List("first","second","third")
      var outputCounter = 0

      override def configure(configuration: FilterConfiguration): Unit = ???

      override def onPull(): Unit = {
        if (outputCounter < input.size) {
          push(shape.out, new Dataset(List(Record(TextField(outputCounter.toString, input(outputCounter))))))
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
        assert(result.records.head.payload.values.head.value.toString.contains("_modified"))
        Console.println(result)
        pull(in)
      }
    }
  }

  val filterLogicProvider = (ctx: StageContext, config: FilterConfiguration, shape: FlowShape[Dataset, Dataset]) => {
    new FilterLogic(ctx, config, shape) {

      override def configure(configuration: FilterConfiguration): Unit = ???

      override def onPush(): Unit = {
        val dataset = grab(in)
        Console.println(dataset)
        val modifiedValue = dataset.records.head.payload.values.head.value +"_modified"
        val newRecord = Record(TextField(dataset.records.head.payload.head._1,modifiedValue))

        push(out, new Dataset(List(newRecord)))
      }

      override def onPull(): Unit = {
        pull(in)
      }
    }
  }
}
