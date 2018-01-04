package io.logbee.keyscore.frontier.filters

case class GrokFilterConfiguration(
  isPaused: Option[Boolean] = None,
  fieldNames: Option[List[String]] = None,
  pattern: Option[String] = None
)
