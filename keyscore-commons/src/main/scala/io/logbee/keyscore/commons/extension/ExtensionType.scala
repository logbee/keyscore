package io.logbee.keyscore.commons.extension


object ExtensionType {
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
case object ExternalExtension extends ExtensionType
