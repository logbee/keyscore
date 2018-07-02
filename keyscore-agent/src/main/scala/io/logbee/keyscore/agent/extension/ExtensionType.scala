package io.logbee.keyscore.agent.extension

object ExtensionType {

  def fromString(name: Option[String]): Option[ExtensionType] = name match {
    case Some(string) =>
      fromString(string)
    case _ =>
      None
  }

  def fromString(name: String): Option[ExtensionType] = name.toLowerCase match {
    case "filter" => Some(FilterExtension)
    case "sink" => Some(SinkExtension)
    case "source" => Some(SourceExtension)
    case "external" => Some(SourceExtension)
    case _ => None
  }
}

trait ExtensionType

case object FilterExtension extends ExtensionType
case object SinkExtension extends ExtensionType
case object SourceExtension extends ExtensionType
case object ExternalFilterExtension extends ExtensionType
