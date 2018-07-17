package io.logbee.keyscore.model.json4s

import org.json4s.Formats
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization

object KeyscoreFormats {

  val formats: Formats = Serialization.formats(KeyscoreTypeHints.all) ++
    JavaTypesSerializers.all ++
    List(HealthSerializer, FilterStatusSerializer)

}
