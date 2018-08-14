package io.logbee.keyscore.model.localization

import io.logbee.keyscore.model.localization.{Locale, TranslationMapping}

trait TranslationMappingCompanion {
  implicit def mapToTranslationMapping(translations: Map[Locale, String]): TranslationMapping = {
    TranslationMapping(translations)
  }
}
