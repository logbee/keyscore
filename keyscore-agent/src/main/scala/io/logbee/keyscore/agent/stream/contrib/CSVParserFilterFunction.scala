package io.logbee.keyscore.agent.stream.contrib

import io.logbee.keyscore.model.{Dataset, Described}
import io.logbee.keyscore.model.filter._

object CSVParserFilterFunction extends Described {
  override def descriptor: FilterDescriptor = FilterDescriptor("CSVFilter", "Filter that parses csv in a readable format with key and value.", List(
    ListParameterDescriptor("headers", TextParameterDescriptor("headerName"), min = 1),
    TextParameterDescriptor("record")
  ))
}

class CSVParserFilterFunction extends FilterFunction {
  override def configure(configuration: FilterConfiguration): Unit = ???

  override def apply(dataset: Dataset): Dataset = ???
}
