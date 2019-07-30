package io.logbee.keyscore.example

import io.logbee.keyscore.model.descriptor.Category
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef, TranslationMapping}
import io.logbee.keyscore.model.util.ToOption.T2OptionT

object ExampleCategory {

  val EXAMPLE = Category("example", TextRef("example.category.displayName"))

  import Locale.{ENGLISH, GERMAN}

  val LOCALIZATION = Localization(
    locales = Set(ENGLISH, GERMAN),
    mapping = Map(
      EXAMPLE.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Example",
        GERMAN -> "Beispiel"
      )),
    )
  )
}