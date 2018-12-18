package io.logbee.keyscore.agent

import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data.{Label, MetaData, TextValue}
import io.logbee.keyscore.model.descriptor.ParameterDescriptorMessage.SealedValue
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.contrib.elasticsearch.ElasticSearchSinkLogic
import io.logbee.keyscore.pipeline.contrib.filter.{RemoveFieldsLogic, RetainFieldsLogic}
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
      parameterSet = ParameterSet(Seq(
        TextParameter(KafkaSourceLogic.serverParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSourceLogic.portParameter.ref, 9092),
        TextParameter(KafkaSourceLogic.groupIdParameter.ref, "groupId"),
        ChoiceParameter(KafkaSourceLogic.offsetParameter.ref, "earliest"),
        TextParameter(KafkaSourceLogic.topicParameter.ref, "TopicA"),
        FieldNameParameter(KafkaSourceLogic.fieldNameParameter.ref, "foo")
      )
      ))

    val removeFieldsFilterConfigurationRef = ConfigurationRef("d2588462-b5f4-4b10-8cbb-7bcceb178cb5")
    val removeFieldsFilterConfig = Configuration(removeFieldsFilterConfigurationRef,
      parameterSet = ParameterSet(Seq(
        FieldNameListParameter(RemoveFieldsLogic.fieldsToRemoveParameter.ref, Seq("message"))
      )))

    val sinkConfigurationRef = ConfigurationRef("05dc6d8a-50ff-41bd-b637-5132be1f2415")
    val sinkConfig = Configuration(sinkConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(KafkaSinkLogic.bootstrapServerParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSinkLogic.bootstrapServerPortParameter.ref, 9092),
        TextParameter(KafkaSinkLogic.topicParameter.ref, "TopicB"),
        FieldNameParameter(KafkaSinkLogic.fieldNameParameter.ref, "foo")
      )
      ))


    val sourceBluePrint = SourceBlueprint(BlueprintRef("d69c8aca-2ceb-49c5-b4f8-f8298e5187cd"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val filterBluePrint = FilterBlueprint(BlueprintRef("24a88215-cfe0-47a1-a889-7f3e9f8260ef"), RemoveFieldsLogic.describe.ref, removeFieldsFilterConfigurationRef)
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
      parameterSet = ParameterSet(Seq(
        TextParameter(KafkaSourceLogic.serverParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSourceLogic.portParameter.ref, 9092),
        TextParameter(KafkaSourceLogic.groupIdParameter.ref, "groupId"),
        ChoiceParameter(KafkaSourceLogic.offsetParameter.ref, "earliest"),
        TextParameter(KafkaSourceLogic.topicParameter.ref, "TopicB"),
        FieldNameParameter(KafkaSourceLogic.fieldNameParameter.ref, "foo")
      )
    ))

    val removeFieldsFilterConfigurationRef = ConfigurationRef("07fbf227-3cde-4acd-853a-4aa733f5f482")
    val removeFieldsFilterConfig = Configuration(removeFieldsFilterConfigurationRef,
      parameterSet = ParameterSet(Seq(
        FieldNameListParameter(RemoveFieldsLogic.fieldsToRemoveParameter.ref, Seq("message"))
      )))

    val sinkConfigurationRef = ConfigurationRef("d35d1f46-cd41-4a25-8d83-02cf9348d87e")
    val sinkConfig = Configuration(sinkConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(ElasticSearchSinkLogic.hostParameter.ref, "keyscore-elasticsearch"),
        NumberParameter(ElasticSearchSinkLogic.portParameter.ref, 9200),
        TextParameter(ElasticSearchSinkLogic.indexParameter.ref, "test")
      )
    ))


    val sourceBluePrint = SourceBlueprint(BlueprintRef("4a696573-ce73-4bb2-8d9a-b2a90e834153"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val filterBluePrint = FilterBlueprint(BlueprintRef("dc882c27-3de2-4603-b272-b35cf81080e2"), RemoveFieldsLogic.describe.ref, removeFieldsFilterConfigurationRef)
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

  trait Workflow {

    val kafkaSourceConfigurationRef = ConfigurationRef("2726cd82-3c85-4a25-91ef-97b13cfad8e6")
    val kafkaSourceConfiguration = Configuration(kafkaSourceConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(KafkaSourceLogic.serverParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSourceLogic.portParameter.ref, 9092),
        TextParameter(KafkaSourceLogic.groupIdParameter.ref, "groupId"),
        ChoiceParameter(KafkaSourceLogic.offsetParameter.ref, "earliest"),
        TextParameter(KafkaSourceLogic.topicParameter.ref, "TopicW1"),
        FieldNameParameter(KafkaSourceLogic.fieldNameParameter.ref, "foo")
      )))

    val retainFieldsConfigurationRef = ConfigurationRef("0584fc3f-b629-4b95-a5d4-ae87fdf01b77")
    val retainFieldsConfiguration = Configuration(retainFieldsConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextListParameter(RetainFieldsLogic.fieldNamesParameter.ref, Seq("text1", "text2", "text3", "number1", "number2"))
      )))

    val firstRemoveFieldsConfigurationRef = ConfigurationRef("dd87ea44-3cfa-4cda-8fc2-9f4869f54338")
    val firstRemoveFieldsConfiguration = Configuration(firstRemoveFieldsConfigurationRef,
      parameterSet = ParameterSet(Seq(
        FieldNameListParameter(RemoveFieldsLogic.fieldsToRemoveParameter.ref, Seq("text1"))
      )))

    val secondRemoveFieldsConfigurationRef = ConfigurationRef("f60f7876-d4b2-4657-bd94-1164424804dc")
    val secondRemoveFieldsConfiguration = Configuration(secondRemoveFieldsConfigurationRef,
      parameterSet = ParameterSet(Seq(
        FieldNameListParameter(RemoveFieldsLogic.fieldsToRemoveParameter.ref, Seq("text2", "number2"))
      )))

    val elasticSinkConfigurationRef = ConfigurationRef("7cce7c4c-c50e-49a8-b0fb-db2c45fb737b")
    val elasticSinkConfiguration = Configuration(elasticSinkConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(ElasticSearchSinkLogic.hostParameter.ref, "keyscore-elasticsearch"),
        NumberParameter(ElasticSearchSinkLogic.portParameter.ref, 9200),
        TextParameter(ElasticSearchSinkLogic.indexParameter.ref, "workflow")
      )))

    val kafkaSourceBlueprint = SourceBlueprint(BlueprintRef("7bd47b18-547f-4752-9f5c-54028a6f5be0"), KafkaSourceLogic.describe.ref, kafkaSourceConfigurationRef)
    val retainFieldsBlueprint = FilterBlueprint(BlueprintRef("f368c58c-db9a-43dc-8ccb-f495d29c441f"), RetainFieldsLogic.describe.ref, retainFieldsConfigurationRef)
    val firstRemoveFieldsBlueprint = FilterBlueprint(BlueprintRef("29c2942f-d098-46fc-a014-50ddc5277c0e"), RemoveFieldsLogic.describe.ref, firstRemoveFieldsConfigurationRef)
    val secondRemoveFieldsBlueprint = FilterBlueprint(BlueprintRef("921a7d13-ebe0-49f8-8fc6-1e9064d1eba9"), RemoveFieldsLogic.describe.ref, secondRemoveFieldsConfigurationRef)
    val elasticSinkBlueprint = SinkBlueprint(BlueprintRef("3dfa3823-e202-4715-ad5d-0f81fc6efbc6"), ElasticSearchSinkLogic.describe.ref, elasticSinkConfigurationRef)

    val workflowPipelineBlueprint = PipelineBlueprint(BlueprintRef("e49abe8a-6bf4-410f-8781-87b36db8168d"), Seq(
      kafkaSourceBlueprint.ref,
      retainFieldsBlueprint.ref,
      firstRemoveFieldsBlueprint.ref,
      secondRemoveFieldsBlueprint.ref,
      elasticSinkBlueprint.ref),
      metadata = MetaData(
        Label("pipeline.name", TextValue("WorkflowPipeline")),
        Label("pipeline.description", TextValue("Some meaningful description:"))
      )
    )
  }

  "A running PipelineSupervisor" should {

    "Generate json files for KafkaToKafka" in new KafkaToKafka {
      println("KafkaToKafka Jsons")
      println(writePretty(sourceBluePrint))
      println(writePretty(sinkBluePrint))
      println(writePretty(filterBluePrint))

      println(writePretty(pipelineBlueprint))

      println(writePretty(sourceConfig))
      println(writePretty(sinkConfig))
      println(writePretty(removeFieldsFilterConfig))

      println(writePretty(KafkaSinkLogic.describe))
      println(writePretty(KafkaSourceLogic.describe))
      println(writePretty(RemoveFieldsLogic.describe))
    }

    "Generate json files for KafkaToElastic" in new KafkaToElastic {
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
      println(writePretty(RemoveFieldsLogic.describe))
    }

    "Generate json files for Workflow" in new Workflow {
      println("Workflow Jsons")
      println("Blueprints")

      println(writePretty(kafkaSourceBlueprint))
      println(writePretty(retainFieldsBlueprint))
      println(writePretty(firstRemoveFieldsBlueprint))
      println(writePretty(secondRemoveFieldsBlueprint))
      println(writePretty(elasticSinkBlueprint))

      println(writePretty(workflowPipelineBlueprint))

      println("Configurations")

      println(writePretty(kafkaSourceConfiguration))
      println(writePretty(retainFieldsConfiguration))
      println(writePretty(firstRemoveFieldsConfiguration))
      println(writePretty(secondRemoveFieldsConfiguration))
      println(writePretty(elasticSinkConfiguration))
    }


  }
}

