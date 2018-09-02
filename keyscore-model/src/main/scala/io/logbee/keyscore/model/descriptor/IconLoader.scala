package io.logbee.keyscore.model.descriptor

import io.logbee.keyscore.model.descriptor.IconEncoding.Base64
import io.logbee.keyscore.model.descriptor.IconFormat.SVG
import io.logbee.keyscore.model.util.Hashing.toHashable
import io.logbee.keyscore.model.util.Using.using

import scala.io.Source
import scala.util.{Success, Try}

trait IconLoader {
  def fromResource(resource: String): Icon = {
    using(getClass.getResourceAsStream(resource))(stream => {
      Try(Source.fromInputStream(stream).mkString)
    }) match {
      case Success(raw) => Icon(
        data = raw.base64(),
        format = SVG,
        encoding = Base64
      )
      case _ => Icon()
    }
  }
}
