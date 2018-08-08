package io.logbee.keyscore.agent.pipeline

import java.util.Locale
import java.util.UUID.randomUUID

import io.logbee.keyscore.agent.pipeline.contrib.filter.{AddFieldsFilterLogic, CSVParserFilterLogic}
import io.logbee.keyscore.agent.pipeline.contrib.kafka.{KafkaSinkLogic, KafkaSourceLogic}
import io.logbee.keyscore.model._
import io.logbee.keyscore.model.filter._

import scala.collection.mutable

object ExampleData {

  val messageTextField1 = Field("message", TextValue("The weather is cloudy with a current temperature of: -11.5 C"))
  val messageTextField2 = Field("message", TextValue("Is is a rainy day. Temperature: 5.8 C"))
  val messageTextField3 = Field("message", TextValue("The weather is sunny with a current temperature of: 14.4 C"))

  val record1 = Record(messageTextField1)
  val record2 = Record(messageTextField2)
  val record3 = Record(messageTextField3)

  val multiFields1 = Record(
    Field("foo", TextValue("bar")),
    Field("bar", TextValue("42")),
    Field("bbq", TextValue("meat")),
    Field("beer", TextValue("non-alcoholic"))
  )

  val multiFields2 = Record(
    Field("foo", TextValue("bar")),
    Field("42", TextValue("bar"))
  )

  //CSV Filter
  val csvA = Record(Field("message", TextValue("13;07;09;15;;;")))
  val csvB = Record(Field("message", TextValue(";03;05;01;;;")))

  //Kafka
  val kafka1 = Record(
    Field("id", TextValue("01")),
    Field("name", TextValue("robo"))
  )
  val kafka2 = Record(
    Field("id", TextValue("02")),
    Field("name", TextValue("logbee"))
  )

  //Modified messages
  val record1Modified = Record(Field("weather-report", TextValue("cloudy, -11.5 C")))
  val record2Modified = Record(Field("weather-report", TextValue("rainy, 5.8 C")))
  val record3Modified = Record(Field("weather-report", TextValue("sunny, 14.4 C")))

  val multiRecordModified = Record(
    Field("bar", TextValue("42")),
    Field("bbq", TextValue("meat"))
  )
  val multiRecordModified2 = Record(
    Field("foo", TextValue("bar"))
  )

  //Original datasets
  val dataset1 = Dataset(MetaData(Label("name", TextValue("dataset1"))), record1)
  val dataset2 = Dataset(MetaData(Label("name", TextValue("dataset2"))), record2)
  val dataset3 = Dataset(MetaData(Label("name", TextValue("dataset3"))), record3)
  val dataset4 = Dataset(MetaData(Label("name", TextValue("dataset4"))), record1, multiFields1)
  val dataset5 = Dataset(MetaData(Label("name", TextValue("dataset5"))), record2, multiFields2)

  val datasetMulti1 = Dataset(multiFields1)
  val datasetMulti2 = Dataset(multiFields2)

  //CSV
  val csvDatasetA = Dataset(csvA)
  val csvDatasetB = Dataset(csvB)

  //Kafka
  val kafkaDataset1 = Dataset(kafka1)
  val kafkaDataset2 = Dataset(kafka2)

  //Modified datasets
  val dataset1Modified = Dataset(record1Modified)
  val dataset2Modified = Dataset(record2Modified)
  val dataset3Modified = Dataset(record3Modified)
  val datasetMultiModified = Dataset(multiRecordModified)
  val datasetMultiModified2 = Dataset(multiRecordModified2)

  // descriptors
  val filterDescriptorA = FilterDescriptor(randomUUID(), "filterA", List.empty)
  val filterDescriptorB = FilterDescriptor(randomUUID(), "filterB", List.empty)

  //configurations
  val configA = FilterConfiguration(filterDescriptorA)
  val configB = FilterConfiguration(filterDescriptorB)

  //CSV
  val csvHeader = FilterConfiguration(randomUUID(), CSVParserFilterLogic.describe.describe(), List(
    TextParameter("separator", ";"),
    TextListParameter("headers", List("Philosophy", "Maths", "Latin", "Astrophysics")))
  )
  val csvHeader2 = FilterConfiguration(randomUUID(), CSVParserFilterLogic.describe.describe(), List(
    TextParameter("separator", ";"),
    TextListParameter("headers", List("Philosophy2", "Maths2", "Latin2", "Astrophysics2")))
  )

  //Kafka
  val kafkaSourceConfiguration = FilterConfiguration(randomUUID(), KafkaSourceLogic.describe.describe(), List(
    TextParameter("bootstrapServer", "localhost:9092"),
    TextParameter("groupID", "keyscore-agent"),
    TextParameter("offsetCommit", "earliest"),
    TextParameter("sourceTopic", "testTopic")
  ))

  val kafkaSinkConfiguration = FilterConfiguration(randomUUID(), KafkaSinkLogic.describe.describe(), List(
    TextParameter("bootstrapServer", "localhost:9092"),
    TextParameter("topic", "sinkTopic")
  ))

  val addFieldsFilterConfiguration = FilterConfiguration(randomUUID(), AddFieldsFilterLogic.describe.describe(Locale.ENGLISH),List(TextMapParameter("test",Map("test"->"test"))))

  var filterList = mutable.ListBuffer[FilterConfiguration]()
  filterList += addFieldsFilterConfiguration

  val pipelineConfiguration = PipelineConfiguration(randomUUID(),"Testpipeline", "Valid PipelineConfiguration for testing",kafkaSourceConfiguration, filterList.toList, kafkaSinkConfiguration)

}

