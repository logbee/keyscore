package io.logbee.keyscore.model.data

import io.logbee.keyscore.model.data.IconEncoding.RAW
import io.logbee.keyscore.model.data.IconFormat.SVG
import io.logbee.keyscore.model.util.Using.using

import scala.io.Source
import scala.util.{Success, Try}

trait IconLoader {

  def fromClass(clazz: Class[_], format: IconFormat = SVG, encoding: IconEncoding = RAW): Icon = {
    using(clazz.getResourceAsStream(s"${clazz.getSimpleName}.${suffix(format)}"))(stream => {
      Try(Source.fromInputStream(stream).mkString)
    }) match {
      case Success(raw) => Icon(
        data = raw,
        format = format,
        encoding = encoding
      )
      case _ => Icon()
    }
  }

  def fromResource(resource: String, format: IconFormat = SVG, encoding: IconEncoding = RAW): Icon = {
    using(getClass.getResourceAsStream(resource))(stream => {
      Try(Source.fromInputStream(stream).mkString)
    }) match {
      case Success(raw) => Icon(
        data = raw,
        format = format,
        encoding = encoding
      )
      case _ => Icon()
    }
  }

  private def suffix(format: IconFormat): String = {
    format.name.toLowerCase
  }
}
