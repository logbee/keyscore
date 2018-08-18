package io.logbee.keyscore.model.localization

import java.util

import scalapb.TypeMapper

trait BaseLocale {

  this: Locale =>

  def asJava: java.util.Locale = new util.Locale(this.language, this.country)
}

trait LocaleCompanion {

  private val separator = "_"

  def apply(locale: java.util.Locale): Locale = locale

  def apply(locale: String): Locale = {
    val split = locale.split(separator)
    if (split.length == 2) {
      Locale(split(0), split(1))
    }
    else {
      Locale(locale, "")
    }
  }

  implicit def localeToString(locale: Locale): String = {
    if (locale.country != null && locale.country.nonEmpty) {
      s"${locale.language}$separator${locale.country}"
    }
    else {
      s"${locale.language}"
    }
  }

  implicit def localeToJavaLocale(locale: Locale): java.util.Locale =  locale.asJava

  implicit def localeFromJavaLocale(locale: java.util.Locale): Locale = Locale(locale.getLanguage, locale.getCountry)

  implicit val typeMapper = TypeMapper[String, Locale](locale => Locale(locale))(locale => locale)
}
