package io.logbee.keyscore.frontier.filters

import java.util.UUID

object GrokFilterConfiguration {

  val GrokFilterConfigurationApply: ((Option[Boolean], Option[List[String]], Option[String]) => GrokFilterConfiguration) = GrokFilterConfiguration.apply

  def apply(isPaused: Boolean, fieldNames: List[String], pattern: String): GrokFilterConfiguration = new GrokFilterConfiguration(Some(isPaused), Some(fieldNames), Some(pattern))

  def apply(isPaused: Boolean, fieldNames: List[String]): GrokFilterConfiguration = new GrokFilterConfiguration(isPaused = Some(isPaused), fieldNames = Some(fieldNames))

  def apply(fieldNames: List[String]): GrokFilterConfiguration = new GrokFilterConfiguration(fieldNames = Some(fieldNames))

  def apply(isPaused: Boolean): GrokFilterConfiguration = new GrokFilterConfiguration(Some(isPaused))
}

case class GrokFilterConfiguration(
  isPaused: Option[Boolean] = None,
  fieldNames: Option[List[String]] = None,
  pattern: Option[String] = None
)
