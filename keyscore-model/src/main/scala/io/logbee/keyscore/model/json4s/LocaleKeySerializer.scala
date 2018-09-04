package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.localization.Locale
import org.json4s.CustomKeySerializer

case object LocaleKeySerializer extends CustomKeySerializer[Locale](format => ({
  case locale: String => Locale(locale)
}, {
  case locale: Locale => Locale.localeToString(locale)
}))