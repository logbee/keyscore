package io.logbee.keyscore.model

trait TranslationMappingCompanion {
  implicit def mapToTranslationMapping(translations: Map[Locale, String]): TranslationMapping = {
    TranslationMapping(translations)
  }
}
