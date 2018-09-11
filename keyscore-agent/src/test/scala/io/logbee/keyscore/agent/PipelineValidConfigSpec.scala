package io.logbee.keyscore.agent

import akka.actor.Props
import akka.testkit.TestProbe
import io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilterLogic
import io.logbee.keyscore.agent.pipeline.contrib.kafka.{KafkaSinkLogic, KafkaSourceLogic}
import io.logbee.keyscore.agent.pipeline.{FilterManager, PipelineSupervisor}
import io.logbee.keyscore.commons.extension.ExtensionLoader.RegisterExtension
import io.logbee.keyscore.commons.extension.FilterExtension
import io.logbee.keyscore.commons.test.ProductionSystemWithMaterializerAndExecutionContext
import io.logbee.keyscore.frontier.cluster.resources.{ConfigurationManager, DescriptorManager}
import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}


class PipelineValidConfigSpec extends ProductionSystemWithMaterializerAndExecutionContext with WordSpecLike with Matchers with ScalaFutures with MockFactory {

  implicit val formats = KeyscoreFormats.formats
  "A running PipelineSupervisor" should {

    val filterManager = system.actorOf(Props[FilterManager], "filterManager")
    val supervisor = system.actorOf(PipelineSupervisor(filterManager))
    val descriptorManager = system.actorOf(DescriptorManager())
    val configurationManager = system.actorOf(ConfigurationManager())
    val agent = TestProbe("agent")


    filterManager ! RegisterExtension(FilterExtension, classOf[KafkaSourceLogic])
    filterManager ! RegisterExtension(FilterExtension, classOf[KafkaSinkLogic])
    filterManager ! RegisterExtension(FilterExtension, classOf[RemoveFieldsFilterLogic])

    val sourceConfigurationRef = ConfigurationRef("bae4e0bc-2784-416a-a93d-0e36ed80d6e0")
    val sourceConfig = Configuration(sourceConfigurationRef,
      parameters = Seq(
        TextParameter(KafkaSourceLogic.serverParameter.ref, "localhost"),
        NumberParameter(KafkaSourceLogic.portParameter.ref, 9092),
        TextParameter(KafkaSourceLogic.groupIdParameter.ref, "groupId"),
        ChoiceParameter(KafkaSourceLogic.offsetParameter.ref, "earliest"),
        TextParameter(KafkaSourceLogic.topicParameter.ref, "TopicA")
      )
    )

    val removeFieldsFilterConfigurationRef = ConfigurationRef("d2588462-b5f4-4b10-8cbb-7bcceb178cb5")
    val removeFieldsFilterConfig = Configuration(removeFieldsFilterConfigurationRef,
      parameters = Seq(
        FieldNameListParameter(RemoveFieldsFilterLogic.fieldsToRemoveParameter.ref, Seq("message"))
      ))

    val sinkConfigurationRef = ConfigurationRef("05dc6d8a-50ff-41bd-b637-5132be1f2415")
    val sinkConfig = Configuration(sinkConfigurationRef,
      parameters = Seq(
        TextParameter(KafkaSinkLogic.bootstrapServerParameter.ref, "localhost"),
        NumberParameter(KafkaSinkLogic.bootstrapServerPortParameter.ref, 9092),
        TextParameter(KafkaSinkLogic.topicParameter.ref, "TopicB")
      )
    )


    val sourceBluePrint = SourceBlueprint(BlueprintRef("d69c8aca-2ceb-49c5-b4f8-f8298e5187cd"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val filterBluePrint = FilterBlueprint(BlueprintRef("24a88215-cfe0-47a1-a889-7f3e9f8260ef"), RemoveFieldsFilterLogic.describe.ref, removeFieldsFilterConfigurationRef)
    val sinkBluePrint = SinkBlueprint(BlueprintRef("80851e06-7191-4d96-8e4d-de66a5a12e81"), KafkaSinkLogic.describe.ref, sinkConfigurationRef)

    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("10d3e280-cb7c-4a77-be1f-8ccf5f1b0df2"), Seq(
      sourceBluePrint,
      filterBluePrint,
      sinkBluePrint),
      name = "IntegrationTestPipeline",
      description = "It's valid"
    )


//    configurationManager ! StoreConfigurationRequest(sourceConfig)
//    configurationManager ! StoreConfigurationRequest(sinkConfig)
//    configurationManager ! StoreConfigurationRequest(removeFieldsFilterConfig)

//    descriptorManager ! StoreDescriptorRequest(KafkaSourceLogic.describe)
//    descriptorManager ! StoreDescriptorRequest(KafkaSinkLogic.describe)
//    descriptorManager ! StoreDescriptorRequest(RemoveFieldsFilterLogic.describe)

//    println(writePretty(sourceBluePrint))
//    println(writePretty(sinkBluePrint))
//    println(writePretty(filterBluePrint))

//    println(writePretty(pipelineBlueprint))
//    println(writePretty(sourceConfig))
//    println(writePretty(sinkConfig))
//    println(writePretty(removeFieldsFilterConfig))

//    println(writePretty(KafkaSinkLogic.describe))
//    println(writePretty(KafkaSourceLogic.describe))
//    println(writePretty(RemoveFieldsFilterLogic.describe))


//    def pollPipelineHealthState(maxRetries: Int, sleepTimeMs: Long): Boolean = {
//      var retries = maxRetries
//      while (retries > 0) {
//
//        supervisor tell(RequestPipelineInstance(agent.ref), agent.ref)
//        val pipelineInstance = agent.receiveOne(2 seconds).asInstanceOf[PipelineInstance]
//        if (pipelineInstance.health.equals(Green)) {
//          return true
//        }
//        Thread.sleep(sleepTimeMs)
//        retries -= 1
//      }
//
//      false
//    }
//
//    "start a pipeline with a working configuration" in {
//      supervisor ! CreatePipeline(pipelineBlueprint)
//
//      supervisor tell(RequestPipelineInstance(agent.ref), agent.ref)
//      agent.expectMsg(PipelineInstance(pipelineBlueprint.ref.uuid, pipelineBlueprint.name, pipelineBlueprint.description, Red))
//
//      pollPipelineHealthState(maxRetries = 10, sleepTimeMs = 2000) shouldBe true
//
//    }
  }
}

