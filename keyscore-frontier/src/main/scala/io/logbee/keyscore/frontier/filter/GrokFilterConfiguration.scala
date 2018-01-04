package io.logbee.keyscore.frontier.filter

case class GrokFilterConfiguration(
  isPaused: Option[Boolean] = Some(false),
  fieldNames: Option[List[String]] = None,
  pattern: Option[String] = None
)
