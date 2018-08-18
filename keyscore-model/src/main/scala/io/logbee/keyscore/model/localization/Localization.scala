package io.logbee.keyscore.model.localization

import java.util.ResourceBundle

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Success, Try}

trait BaseLocalization {

  this: Localization =>

  def ++(other: Localization): Localization = {
    Localization(this.locales ++ other.locales, this.mapping ++ other.mapping)
  }
}

trait LocalizationCompanion {

  def fromResourceBundle(bundleName: String, locales: Locale*): Localization = {
    fromResourceBundle(bundleName, locales.toSet)
  }

  def fromResourceBundle(bundleName: String, locales: Set[Locale]): Localization = {

    val resourceBundles = locales.foldLeft(Map.empty[Locale, ResourceBundle]) {
      case (result, locale) => Try(ResourceBundle.getBundle(bundleName, locale)) match {
        case Success(bundle) => result + (locale -> bundle)
        case _ => result
      }
    }

    val textRefs = resourceBundles.values.foldLeft(Set.empty[TextRef]) {
      case (result, bundle) => result ++ bundle.getKeys.asScala.map(TextRef(_))
    }

    val mappings = textRefs.foldLeft(Map.empty[TextRef, TranslationMapping]) {
      case (result, ref) =>
        result + (ref -> TranslationMapping(resourceBundles.map {
          case (locale, bundle) => locale -> bundle.getString(ref.id)
        }))
    }

    Localization(resourceBundles.keys.toSet, mappings)
  }

  def fromMapping(mapping: (TextRef, Map[Locale, String])*): Localization = {

    val locales = mutable.Set.empty[Locale]
    val translations = mapping.foldLeft(Map.empty[TextRef, TranslationMapping]) {
      case (result, (ref, translations)) =>
        translations.keys.foreach(locales.add)
        result + (ref -> TranslationMapping(translations))
    }

    Localization(locales.toSet, translations)
  }

  def fromJavaMapping(mapping: (TextRef, Map[java.util.Locale, String])*): Localization = {

    val locales = mutable.Set.empty[Locale]
    val translations = mapping.foldLeft(Map.empty[TextRef, TranslationMapping]) {
      case (result, (ref, translations)) =>
        val converted = translations.map {
          case (locale, text) => Locale(locale) -> text
        }
        converted.keys.foreach(locales.add)
        result + (ref -> TranslationMapping(converted))
    }

    Localization(locales.toSet, translations)
  }
}
