package io.logbee.keyscore.agent

import io.logbee.keyscore.model.blueprint._
import io.logbee.keyscore.model.configuration._
import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.contrib.DiscardSinkLogic._
import io.logbee.keyscore.pipeline.contrib.MetricSourceLogic._
import io.logbee.keyscore.pipeline.contrib.decoder.json.JsonDecoderLogic
import io.logbee.keyscore.pipeline.contrib.elasticsearch.ElasticSearchSinkLogic
import io.logbee.keyscore.pipeline.contrib.encoder.json.JsonEncoderLogic
import io.logbee.keyscore.pipeline.contrib.encoder.json.JsonEncoderLogic._
import io.logbee.keyscore.pipeline.contrib.filter.AddFieldsLogic.fieldListParameter
import io.logbee.keyscore.pipeline.contrib.filter.{AddFieldsLogic, RemoveFieldsLogic, RetainFieldsLogic}
import io.logbee.keyscore.pipeline.contrib.kafka.{KafkaSinkLogic, KafkaSourceLogic}
import io.logbee.keyscore.pipeline.contrib.{DiscardSinkLogic, MetricSourceLogic}
import io.logbee.keyscore.test.fixtures.ProductionSystemWithMaterializerAndExecutionContext
import org.json4s.native.Serialization.writePretty
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PipelineValidConfigSpec extends ProductionSystemWithMaterializerAndExecutionContext with AnyWordSpecLike with Matchers with ScalaFutures with MockFactory {

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
        FieldNameParameter(KafkaSourceLogic.fieldNameParameter.ref, "__data_")
      )))

    val jsonDecoderConfigurationRef = ConfigurationRef("99ad36c0-1c34-42e8-8001-41d1c4a54d85")
    val jsonDecoderConfig = Configuration(jsonDecoderConfigurationRef,
      parameterSet = ParameterSet(Seq(
        BooleanParameter(JsonDecoderLogic.removeSourceFieldParameter.ref, true),
        TextParameter(JsonDecoderLogic.sourceFieldNameParameter.ref, "__data_")
      )))

    val removeFieldsFilterConfigurationRef = ConfigurationRef("d2588462-b5f4-4b10-8cbb-7bcceb178cb5")
    val removeFieldsFilterConfig = Configuration(removeFieldsFilterConfigurationRef,
      parameterSet = ParameterSet(Seq(
        FieldNameListParameter(RemoveFieldsLogic.fieldsToRemoveParameter.ref, Seq("message"))
      )))

    val jsonEncoderConfigurationRef = ConfigurationRef("e6609c8c-9a53-4af7-a0d9-3016680b3e99")
    val jsonEncoderConfig = Configuration(jsonEncoderConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(fieldNameParameter.ref, "__data_"),
        ChoiceParameter(batchStrategyParameter.ref, KEEP_BATCH)
      )))

    val sinkConfigurationRef = ConfigurationRef("05dc6d8a-50ff-41bd-b637-5132be1f2415")
    val sinkConfig = Configuration(sinkConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(KafkaSinkLogic.bootstrapServerParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSinkLogic.bootstrapServerPortParameter.ref, 9092),
        TextParameter(KafkaSinkLogic.topicParameter.ref, "TopicB"),
        FieldNameParameter(KafkaSinkLogic.fieldNameParameter.ref, "__data_")
      )))


    val sourceBluePrint = SourceBlueprint(BlueprintRef("d69c8aca-2ceb-49c5-b4f8-f8298e5187cd"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val jsonDecoderBluePrint = FilterBlueprint(BlueprintRef("484b22d0-1d6e-46c4-94ac-9662ff20ee1d"), JsonDecoderLogic.describe.ref, jsonDecoderConfigurationRef)
    val removeFieldsBluePrint = FilterBlueprint(BlueprintRef("24a88215-cfe0-47a1-a889-7f3e9f8260ef"), RemoveFieldsLogic.describe.ref, removeFieldsFilterConfigurationRef)
    val jsonEncoderBluePrint = FilterBlueprint(BlueprintRef("735db838-a6fb-40a1-a53b-6a0749d9a4e8"), JsonEncoderLogic.describe.ref, jsonEncoderConfigurationRef)
    val sinkBluePrint = SinkBlueprint(BlueprintRef("80851e06-7191-4d96-8e4d-de66a5a12e81"), KafkaSinkLogic.describe.ref, sinkConfigurationRef)

    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("10d3e280-cb7c-4a77-be1f-8ccf5f1b0df2"), Seq(
      sourceBluePrint.ref,
      jsonDecoderBluePrint.ref,
      removeFieldsBluePrint.ref,
      jsonEncoderBluePrint.ref,
      sinkBluePrint.ref),
      metadata = MetaData(
        Label("pipeline.name", TextValue("Kafka-To-Kafka")),
        Label("pipeline.description", TextValue("Ships from TopicA to TopicB"))
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
        FieldNameParameter(KafkaSourceLogic.fieldNameParameter.ref, "__data_")
      )))

    val jsonDecoderConfigurationRef = ConfigurationRef("840df94d-ed20-47ca-b061-6daceb8cb747")
    val jsonDecoderConfig = Configuration(jsonDecoderConfigurationRef,
      parameterSet = ParameterSet(Seq(
        BooleanParameter(JsonDecoderLogic.removeSourceFieldParameter.ref, true),
        TextParameter(JsonDecoderLogic.sourceFieldNameParameter.ref, "__data_")
      )))

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
      )))


    val sourceBluePrint = SourceBlueprint(BlueprintRef("4a696573-ce73-4bb2-8d9a-b2a90e834153"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val jsonDecoderBluePrint = FilterBlueprint(BlueprintRef("af14c4a5-9770-4375-b57d-e82bf7ce9afe"), JsonDecoderLogic.describe.ref, jsonDecoderConfigurationRef)
    val removeFieldsBluePrint = FilterBlueprint(BlueprintRef("dc882c27-3de2-4603-b272-b35cf81080e2"), RemoveFieldsLogic.describe.ref, removeFieldsFilterConfigurationRef)
    val sinkBluePrint = SinkBlueprint(BlueprintRef("0f7b4607-b60a-46b8-a396-424466b7618b"), ElasticSearchSinkLogic.describe.ref, sinkConfigurationRef)

    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("34db6f58-0090-4b7d-b32c-aba2706a58bf"), Seq(
      sourceBluePrint.ref,
      jsonDecoderBluePrint.ref,
      removeFieldsBluePrint.ref,
      sinkBluePrint.ref),
      metadata = MetaData(
        Label("pipeline.name", TextValue("Kafka-To-Elastic")),
        Label("pipeline.description", TextValue("Ships from TopicA to index: test"))
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

  trait Metrics {

    //Configurations
    val sourceConfigurationRef = ConfigurationRef("3496a84b-4bf0-494a-8a19-bb081a6d0803")
    val sourceConfig = Configuration(sourceConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(KafkaSourceLogic.serverParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSourceLogic.portParameter.ref, 9092),
        TextParameter(KafkaSourceLogic.groupIdParameter.ref, "groupId"),
        ChoiceParameter(KafkaSourceLogic.offsetParameter.ref, "earliest"),
        TextParameter(KafkaSourceLogic.topicParameter.ref, "Topic1"),
        FieldNameParameter(KafkaSourceLogic.fieldNameParameter.ref, "__data_")
      )))

    val jsonDecoderConfigurationRef = ConfigurationRef("6da87d75-d90f-4601-a633-c56f67164048")
    val jsonDecoderConfig = Configuration(jsonDecoderConfigurationRef,
      parameterSet = ParameterSet(Seq(
        BooleanParameter(JsonDecoderLogic.removeSourceFieldParameter.ref, true),
        TextParameter(JsonDecoderLogic.sourceFieldNameParameter.ref, "__data_")
      )))

    val addFieldsConfigurationRef = ConfigurationRef("da345c10-0ddc-422a-8793-b33d595525d5")
    val addFieldsConfig = Configuration(addFieldsConfigurationRef,
      parameterSet = ParameterSet(Seq(
        FieldListParameter(fieldListParameter.ref, Seq(
          Field("first_added", TextValue("hitchhiker")),
          Field("second_added", NumberValue(42L))
        )))))

    val retainFieldsConfigurationRef = ConfigurationRef("08fe9d53-bdb1-4c20-89be-b63aba621602")
    val retainFieldsConfig = Configuration(retainFieldsConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextListParameter(RetainFieldsLogic.fieldNamesParameter.ref, Seq("text", "number", "first_added", "second_added"))
      )))

    val removeFieldsConfigurationRef = ConfigurationRef("5342872a-2195-4af8-8b57-39ebcf0c6a0a")
    val removeFieldsConfig = Configuration(removeFieldsConfigurationRef,
      parameterSet = ParameterSet(Seq(
        FieldNameListParameter(RemoveFieldsLogic.fieldsToRemoveParameter.ref, Seq("first_added","second_added"))
      )))

    val jsonEncoderConfigurationRef = ConfigurationRef("df35866f-01d8-4de7-889a-721d5d115b42")
    val jsonEncoderConfig = Configuration(jsonEncoderConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(fieldNameParameter.ref, "__data_"),
        ChoiceParameter(batchStrategyParameter.ref, KEEP_BATCH)
      )))

    val sinkConfigurationRef = ConfigurationRef("cf4a508b-1383-4ef3-8b5c-3d018d49b158")
    val sinkConfig = Configuration(sinkConfigurationRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(KafkaSinkLogic.bootstrapServerParameter.ref, "keyscore-kafka"),
        NumberParameter(KafkaSinkLogic.bootstrapServerPortParameter.ref, 9092),
        TextParameter(KafkaSinkLogic.topicParameter.ref, "Topic2"),
        FieldNameParameter(KafkaSinkLogic.fieldNameParameter.ref, "__data_")
      )))


    //Blueprints
    val sourceBlueprint = SourceBlueprint(BlueprintRef("19665f08-7117-47c2-83c9-a99c069d6edc"), KafkaSourceLogic.describe.ref, sourceConfigurationRef)
    val jsonDecoderBlueprint = FilterBlueprint(BlueprintRef("1d8d0973-d9ed-418c-917c-5f4c18fd51e7"), JsonDecoderLogic.describe.ref, jsonDecoderConfigurationRef)
    val addFieldsBlueprint = FilterBlueprint(BlueprintRef("a2912661-7ce2-40d3-b490-d6c58a5cb70f"), AddFieldsLogic.describe.ref, addFieldsConfigurationRef)
    val retainFieldsBlueprint = FilterBlueprint(BlueprintRef("e3d82c2a-9bfd-4118-93aa-11c1b1dfaf82"), RetainFieldsLogic.describe.ref, retainFieldsConfigurationRef)
    val removeFieldsBlueprint = FilterBlueprint(BlueprintRef("34402c9c-09bb-4fc8-be8f-70a513ed6d66"), RemoveFieldsLogic.describe.ref, removeFieldsConfigurationRef)
    val jsonEncoderBlueprint = FilterBlueprint(BlueprintRef("4f7ed3f3-b6b3-489c-ade6-f7cd09fb0197"), JsonEncoderLogic.describe.ref, jsonEncoderConfigurationRef)
    val sinkBlueprint = SinkBlueprint(BlueprintRef("23482214-664e-4298-8e0e-533a68e19e35"), KafkaSinkLogic.describe.ref, sinkConfigurationRef)

    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("7063b8fe-3abe-4513-a53a-da608ff75cac"), Seq(
      sourceBlueprint.ref,
      jsonDecoderBlueprint.ref,
      addFieldsBlueprint.ref,
      retainFieldsBlueprint.ref,
      removeFieldsBlueprint.ref,
      jsonEncoderBlueprint.ref,
      sinkBlueprint.ref),
      metadata = MetaData(
        Label("pipeline.name", TextValue("Metrics Pipeline")),
        Label("pipeline.description", TextValue("For checking metrics."))
      )
    )
  }

  trait MetricSource {

    //1.1 MetricSource Config
    val metricSourceConfigRef = ConfigurationRef("9001fb36-dc17-4042-83e2-adfb49c0bf10")
    val metricSourceConfig = Configuration(metricSourceConfigRef,
      parameterSet = ParameterSet(Seq(
        TextParameter(urlParameter.ref, "http://localhost:4711"),
        TextListParameter(idsParameter.ref, Seq("")),
        NumberParameter(limitParameter.ref, 100),
        TextParameter(earliestParameter.ref, "01.01.2000 00:00:00"),
        TextParameter(latestParameter.ref, "31.12.9999 23:59:59"),
      ))
    )

    //1.2 DiscardSink Config
    val discardSinkConfigRef = ConfigurationRef("03f3364b-415b-4b81-9c3e-1bb252131742")
    val discardSinkConfig = Configuration(discardSinkConfigRef,
      parameterSet = ParameterSet(Seq(
        NumberParameter(intervalParameter.ref, 0)
      ))
    )

    //2.1 MetricSource Blueprint
    val metricSourceBlueprint = SourceBlueprint(BlueprintRef("0d88c536-0a16-4f42-8975-029d84618f44"), MetricSourceLogic.describe.ref, metricSourceConfigRef)

    //2.2 DiscardSink Blueprint
    val discardSinkBlueprint = SinkBlueprint(BlueprintRef("2b3bd90b-fac3-4d90-9197-1e67a600451b"), DiscardSinkLogic.describe.ref, discardSinkConfigRef)

    //2.3 Pipeline Blueprint
    val pipelineBlueprint = PipelineBlueprint(BlueprintRef("c23a40a9-b350-4e5d-86f4-a2e6fe85a648"), Seq(
      metricSourceBlueprint.ref,
      discardSinkBlueprint.ref),
      metadata = MetaData(
        Label("pipeline.name", TextValue("MetricsSource Pipeline")),
        Label("pipeline.description", TextValue("For testing the MetricSource."))
      )
    )

  }

  "A running PipelineSupervisor" should {

    "Generate json files for KafkaToKafka" in new KafkaToKafka {
      println("KafkaToKafka JSONs")
      println(writePretty(sourceBluePrint))
      println(writePretty(jsonDecoderBluePrint))
      println(writePretty(removeFieldsBluePrint))
      println(writePretty(jsonEncoderBluePrint))
      println(writePretty(sinkBluePrint))

      println(writePretty(pipelineBlueprint))

      println(writePretty(sourceConfig))
      println(writePretty(jsonDecoderConfig))
      println(writePretty(removeFieldsFilterConfig))
      println(writePretty(jsonEncoderConfig))
      println(writePretty(sinkConfig))

      println(writePretty(KafkaSourceLogic.describe))
      println(writePretty(JsonDecoderLogic.describe))
      println(writePretty(RemoveFieldsLogic.describe))
      println(writePretty(JsonEncoderLogic.describe))
      println(writePretty(KafkaSinkLogic.describe))
    }

    "Generate json files for KafkaToElastic" in new KafkaToElastic {
      println("KafkaToElastic JSONs")

      println(writePretty(sourceBluePrint))
      println(writePretty(jsonDecoderBluePrint))
      println(writePretty(removeFieldsBluePrint))
      println(writePretty(sinkBluePrint))

      println(writePretty(pipelineBlueprint))

      println(writePretty(sourceConfig))
      println(writePretty(jsonDecoderConfig))
      println(writePretty(removeFieldsFilterConfig))
      println(writePretty(sinkConfig))

      println(writePretty(KafkaSourceLogic.describe))
      println(writePretty(JsonDecoderLogic.describe))
      println(writePretty(RemoveFieldsLogic.describe))
      println(writePretty(KafkaSinkLogic.describe))
    }

    "Generate json files for Workflow" in new Workflow {
      println("Workflow JSONs")
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

    "Generate json files for Metrics" in new Metrics {
      println("Metrics JSONs")

      println(writePretty(sourceBlueprint))
      println(writePretty(jsonDecoderBlueprint))
      println(writePretty(addFieldsBlueprint))
      println(writePretty(retainFieldsBlueprint))
      println(writePretty(removeFieldsBlueprint))
      println(writePretty(jsonEncoderBlueprint))
      println(writePretty(sinkBlueprint))

      println(writePretty(pipelineBlueprint))

      println(writePretty(sourceConfig))
      println(writePretty(jsonDecoderConfig))
      println(writePretty(addFieldsConfig))
      println(writePretty(retainFieldsConfig))
      println(writePretty(removeFieldsConfig))
      println(writePretty(jsonEncoderConfig))
      println(writePretty(sinkConfig))
    }

    "Generate json files for MetricSource" in new MetricSource {
      println(writePretty(metricSourceBlueprint))
      println(writePretty(discardSinkBlueprint))

      println(writePretty(pipelineBlueprint))

      println(writePretty(metricSourceConfig))
      println(writePretty(discardSinkConfig))
    }
  }
}

