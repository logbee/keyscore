package io.logbee.keyscore.agent.stream

import io.logbee.keyscore.model.filter.FilterConfiguration
import io.logbee.keyscore.model.{Dataset, Record, TextField}

object ExampleData {

  val record1 = Record(TextField("message", "The weather is cloudy with a current temperature of: -11.5 °C"))
  val record2 = Record(TextField("message", "Is is a rainy day. Temperature: 5.8 °C"))
  val record3 = Record(TextField("message", "The weather is sunny with a current temperature of: 14.4 °C"))
  val multiRecord = Record(
    TextField("foo", "bar"),
    TextField("42", "bar"),
    TextField("bbq", "meat"),
    TextField("beer", "non-alcoholic")
  )
  val multiRecord2 = Record(
    TextField("foo", "bar"),
    TextField("42", "bar")
  )
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

  val dataset1 = Dataset(record1)
  val dataset2 = Dataset(record2)
  val dataset3 = Dataset(record3)
  val datasetMulti = Dataset(multiRecord)
  val datasetMulti2 = Dataset(multiRecord2)
  val dataset1Modified = Dataset(record1Modified)
  val dataset2Modified = Dataset(record2Modified)
  val dataset3Modified = Dataset(record3Modified)
  val datasetMultiModified = Dataset(multiRecordModified)
  val datasetMultiModified2 = Dataset(multiRecordModified2)

  val configA = FilterConfiguration("A")
  val configB = FilterConfiguration("B")
}
