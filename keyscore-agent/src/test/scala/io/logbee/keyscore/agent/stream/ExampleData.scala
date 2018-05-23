package io.logbee.keyscore.agent.stream

import java.util.UUID.randomUUID

import io.logbee.keyscore.agent.stream.contrib.filter.CSVParserFilterLogic
import io.logbee.keyscore.agent.stream.contrib.kafka.{KafkaSinkLogic, KafkaSourceLogic}
import io.logbee.keyscore.model.filter.{FilterConfiguration, FilterDescriptor, TextListParameter, TextParameter}
import io.logbee.keyscore.model.{Dataset, Record, TextField}

object ExampleData {

  //Original messages
  val record1 = Record(TextField("message", "The weather is cloudy with a current temperature of: -11.5 °C"))
  val record2 = Record(TextField("message", "Is is a rainy day. Temperature: 5.8 °C"))
  val record3 = Record(TextField("message", "The weather is sunny with a current temperature of: 14.4 °C"))

  val multiFields1 = Record(
    TextField("foo", "bar"),
    TextField("bar", "42"),
    TextField("bbq", "meat"),
    TextField("beer", "non-alcoholic")
  )

  val multiFields2 = Record(
    TextField("foo", "bar"),
    TextField("42", "bar")
  )

  //CSV Filter
  val csvA = Record(TextField("message", "13;07;09;15;;;"))
  val csvB = Record(TextField("message", ";03;05;01;;;"))

  //Kafka
  val kafka1 = Record(
    TextField("id", "01"),
    TextField("name", "robo")
  )
  val kafka2 = Record(
    TextField("id", "02"),
    TextField("name", "logbee")
  )

  //Modified messages
  val record1Modified = Record(TextField("weather-report", "cloudy, -11.5 °C"))
  val record2Modified = Record(TextField("weather-report", "rainy, 5.8 °C"))
  val record3Modified = Record(TextField("weather-report", "sunny, 14.4 °C"))
  val multiRecordModified = Record(
    TextField("42", "bar"),
    TextField("bbq", "meat")
  )
  val multiRecordModified2 = Record(
    TextField("42", "bar")
  )

  //Original datasets
  val dataset1 = Dataset(record1)
  val dataset2 = Dataset(record2)
  val dataset3 = Dataset(record3)
  val dataset4 = Dataset(record1, multiFields1)
  val dataset5 = Dataset(record2, multiFields2)

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
    TextListParameter("headers", List("Philosophy","Maths","Latin","Astrophysics")))
  )
  val csvHeader2 = FilterConfiguration(randomUUID(), CSVParserFilterLogic.describe.describe(), List(
    TextParameter("separator", ";"),
    TextListParameter("headers", List("Philosophy2","Maths2","Latin2","Astrophysics2")))
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
}

