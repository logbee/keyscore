package io.logbee.keyscore.agent.pipeline

//TODO Generate this dynamically
object FilterRegistry {

  val filters = Map(
    "KafkaSource" -> "io.logbee.keyscore.frontier.sources.KafkaSource",
    "HttpSource" -> "io.logbee.keyscore.frontier.sources.HttpSource",
    "KafkaSink" -> "io.logbee.keyscore.frontier.sinks.KafkaSink",
    "StdOutSink" -> "io.logbee.keyscore.frontier.sinks.StdOutSink",
    "CSVFilter" -> "io.logbee.keyscore.agent.pipeline.contrib.filter.CSVParserFilterFunction",
    "AddFieldsFilter" -> "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterFunction",
    "RemoveFieldsFilter" -> "io.logbee.keyscore.agent.pipeline.contrib.filter.RemoveFieldsFilterFunction",
    "RetainFieldsFilter" -> "io.logbee.keyscore.agent.pipeline.contrib.filter.RetainFieldsFilterFunction",
    "GrokFilter" -> "io.logbee.keyscore.agent.pipeline.contrib.filter.GrokFilterFunction"
  )
}
