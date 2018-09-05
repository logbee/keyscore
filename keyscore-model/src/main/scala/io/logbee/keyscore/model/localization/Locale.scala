package io.logbee.keyscore.model.localization

import java.util

import scalapb.TypeMapper

trait BaseLocale {

  this: Locale =>

  def asJava: java.util.Locale = new util.Locale(this.language, this.country)
}

trait LocaleCompanion {

  private val separator = "_"

  val ENGLISH = Locale("en")
  val GERMAN = Locale("de")

  def apply(locale: java.util.Locale): Locale = Locale(locale.getLanguage, locale.getCountry)

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

  implicit val typeMapper = TypeMapper[String, Locale](locale => Locale(locale))(locale => locale)
}
