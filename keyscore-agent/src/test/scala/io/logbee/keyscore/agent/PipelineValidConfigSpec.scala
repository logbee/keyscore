package io.logbee.keyscore.agent

import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data.{Label, MetaData, TextValue}
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.contrib.elasticsearch.ElasticSearchSinkLogic
import io.logbee.keyscore.pipeline.contrib.filter.RemoveFieldsFilterLogic
import io.logbee.keyscore.pipeline.contrib.kafka.{KafkaSinkLogic, KafkaSourceLogic}
import io.logbee.keyscore.test.fixtures.ProductionSystemWithMaterializerAndExecutionContext
import org.json4s.native.Serialization.writePretty
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

class PipelineValidConfigSpec extends ProductionSystemWithMaterializerAndExecutionContext with WordSpecLike with Matchers with ScalaFutures with MockFactory {

  implicit val formats = KeyscoreFormats.formats

  trait KafkaToKafka {
    val sourceConfigurationRef = ConfigurationRef("bae4e0bc-2784-416a-a93d-0e36ed80d6e0")
    val sourceConfig = Configuration(sourceConfigurationRef,
      parameters = Seq(
        TextParameter(KafkaSourceLogic.serverParameter.ref, "keyscore-kafka"),
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
        TextParameter(KafkaSinkLogic.bootstrapServerParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSinkLogic.bootstrapServerPortParameter.ref, 9092),
        TextParameter(KafkaSinkLogic.topicParameter.ref, "TopicB")
      )
    )


    val sourceBluePrint = SourceBlueprint(BlueprintRef("d69c8aca-2ceb-49c5-b4f8-f8298e5187cd"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val filterBluePrint = FilterBlueprint(BlueprintRef("24a88215-cfe0-47a1-a889-7f3e9f8260ef"), RemoveFieldsFilterLogic.describe.ref, removeFieldsFilterConfigurationRef)
    val sinkBluePrint = SinkBlueprint(BlueprintRef("80851e06-7191-4d96-8e4d-de66a5a12e81"), KafkaSinkLogic.describe.ref, sinkConfigurationRef)

    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("10d3e280-cb7c-4a77-be1f-8ccf5f1b0df2"), Seq(
      sourceBluePrint.ref,
      filterBluePrint.ref,
      sinkBluePrint.ref),
      metadata = MetaData(
        Label("pipeline.name", TextValue("IntegrationTestPipeline")),
        Label("pipeline.description", TextValue("It's valid"))
      )
    )
  }

  trait KafkaToElastic {
    val sourceConfigurationRef = ConfigurationRef("83094e3e-ec35-4c99-8411-c06271e38591")
    val sourceConfig = Configuration(sourceConfigurationRef,
      parameters = Seq(
        TextParameter(KafkaSourceLogic.serverParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSourceLogic.portParameter.ref, 9092),
        TextParameter(KafkaSourceLogic.groupIdParameter.ref, "groupId"),
        ChoiceParameter(KafkaSourceLogic.offsetParameter.ref, "earliest"),
        TextParameter(KafkaSourceLogic.topicParameter.ref, "TopicB")
      )
    )

    val removeFieldsFilterConfigurationRef = ConfigurationRef("07fbf227-3cde-4acd-853a-4aa733f5f482")
    val removeFieldsFilterConfig = Configuration(removeFieldsFilterConfigurationRef,
      parameters = Seq(
        FieldNameListParameter(RemoveFieldsFilterLogic.fieldsToRemoveParameter.ref, Seq("message"))
      ))

    val sinkConfigurationRef = ConfigurationRef("d35d1f46-cd41-4a25-8d83-02cf9348d87e")
    val sinkConfig = Configuration(sinkConfigurationRef,
      parameters = Seq(
        TextParameter("host", "keyscore-elasticsearch"),
        NumberParameter("port", 9200),
        TextParameter("index", "test")
      )
    )


    val sourceBluePrint = SourceBlueprint(BlueprintRef("4a696573-ce73-4bb2-8d9a-b2a90e834153"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val filterBluePrint = FilterBlueprint(BlueprintRef("dc882c27-3de2-4603-b272-b35cf81080e2"), RemoveFieldsFilterLogic.describe.ref, removeFieldsFilterConfigurationRef)
    val sinkBluePrint = SinkBlueprint(BlueprintRef("0f7b4607-b60a-46b8-a396-424466b7618b"), ElasticSearchSinkLogic.describe.ref, sinkConfigurationRef)

    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("34db6f58-0090-4b7d-b32c-aba2706a58bf"), Seq(
      sourceBluePrint.ref,
      filterBluePrint.ref,
      sinkBluePrint.ref),
      metadata = MetaData(
        Label("pipeline.name", TextValue("IntegrationTestPipeline")),
        Label("pipeline.description", TextValue("It's valid"))
      )
    )
  }

  "A running PipelineSupervisor" should {

//    "Generate json files for KafkaToKafka" in new KafkaToKafka {
//      println("KafkaToKafka Jsons")
//      println(writePretty(sourceBluePrint))
//      println(writePretty(sinkBluePrint))
//      println(writePretty(filterBluePrint))
//
//      println(writePretty(pipelineBlueprint))
//
//      println(writePretty(sourceConfig))
//      println(writePretty(sinkConfig))
//      println(writePretty(removeFieldsFilterConfig))
//
//      println(writePretty(KafkaSinkLogic.describe))
//      println(writePretty(KafkaSourceLogic.describe))
//      println(writePretty(RemoveFieldsFilterLogic.describe))
//    }

        "Generate json files KafkaToElastic" in new KafkaToElastic {
        println("KafkaToElastic Jsons")

          println(writePretty(sourceBluePrint))
          println(writePretty(sinkBluePrint))
          println(writePretty(filterBluePrint))

          println(writePretty(pipelineBlueprint))

          println(writePretty(sourceConfig))
          println(writePretty(sinkConfig))
          println(writePretty(removeFieldsFilterConfig))

          println(writePretty(KafkaSinkLogic.describe))
          println(writePretty(KafkaSourceLogic.describe))
          println(writePretty(RemoveFieldsFilterLogic.describe))
        }


  }
}

