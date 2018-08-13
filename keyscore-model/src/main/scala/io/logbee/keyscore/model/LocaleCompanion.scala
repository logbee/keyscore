package io.logbee.keyscore.model

import scalapb.TypeMapper

trait LocaleCompanion {

  def apply(locale: String): Locale = {
    val split = locale.split("/")
    if (split.length == 2) {
      Locale(split(0), split(1))
    }
    else {
      Locale(locale, "")
    }
  }

  implicit def localeToString(locale: Locale): String = {
    if (locale.country != null && locale.country.nonEmpty) {
      s"${locale.language}/${locale.country}"
    }
    else {
      s"${locale.language}"
    }
  }

  implicit val typeMapper = TypeMapper[String, Locale](locale => Locale(locale))(locale => locale)
}
