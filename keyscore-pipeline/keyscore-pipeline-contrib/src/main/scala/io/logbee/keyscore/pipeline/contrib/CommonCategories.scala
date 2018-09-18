package io.logbee.keyscore.pipeline.contrib

import io.logbee.keyscore.model.descriptor.Category
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef, TranslationMapping}
import io.logbee.keyscore.model.util.ToOption.T2OptionT

object CommonCategories {

  val AUGMENT = Category("contrib.augment", TextRef("contrib.category.augment.displayName"))
  val BATCH_COMPOSITION = Category("contrib.batch-composition", TextRef("contrib.category.batch-composition.displayName"))
  val DATA_EXTRACTION = Category("contrib.data-extraction", TextRef("contrib.category.data-extraction.displayName"))
  val DEBUG = Category("contrib.debug", TextRef("contrib.category.debug.displayName"))
  val FIELDS = Category("contrib.fields", TextRef("contrib.category.fields.displayName"))
  val JSON = Category("contrib.json", TextRef("contrib.category.json.displayName"))
  val MATH = Category("contrib.math", TextRef("contrib.category.math.displayName"))
  val REMOVE_DROP = Category("contrib.remove-drop", TextRef("contrib.category.remove-drop.displayName"))
  val SINK = Category("contrib.source", TextRef("contrib.category.sink.displayName"))
  val SOURCE = Category("contrib.source", TextRef("contrib.category.source.displayName"))
  val VISUALIZATION = Category("contrib.visualization", TextRef("contrib.category.visualization.displayName"))

  import Locale.{ENGLISH, GERMAN}

  val CATEGORY_LOCALIZATION = Localization(
    locales = Set(ENGLISH, GERMAN),
    mapping = Map(
      AUGMENT.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Augment",
        GERMAN -> "Anreichern"
      )),
      BATCH_COMPOSITION.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Batch-Composition",
        GERMAN -> "Stapelbildung"
      )),
      DATA_EXTRACTION.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Data-Extraction",
        GERMAN -> "Datengewinnung"
      )),
      DEBUG.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Debug",
        GERMAN -> "Debug"
      )),
      FIELDS.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Fields",
        GERMAN -> "Felder"
      )),

      JSON.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "JSON",
        GERMAN -> "JSON"
      )),
      MATH.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Math",
        GERMAN -> "Mathematik"
      )),
      REMOVE_DROP.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Remove/Drop",
        GERMAN -> "Entfernen/Verwerfen"
      )),
      SINK.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Sink",
        GERMAN -> "Senke"
      )),
      SOURCE.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Source",
        GERMAN -> "Quelle"
      )),
      VISUALIZATION.displayName.get -> TranslationMapping(Map(
        ENGLISH -> "Visualization",
        GERMAN -> "Visualisierung"
      ))
    )
  )
}
