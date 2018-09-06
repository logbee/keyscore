package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.configuration.ConfigurationRef
import org.json4s.CustomKeySerializer

object ConfigurationRefKeySerializer extends CustomKeySerializer[ConfigurationRef](format => ({
  case uuid: String => ConfigurationRef(uuid)
}, {
  case ref: ConfigurationRef => ref.uuid
}))
