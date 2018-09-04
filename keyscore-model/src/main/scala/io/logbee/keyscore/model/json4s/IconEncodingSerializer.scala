package io.logbee.keyscore.model.json4s

import io.logbee.keyscore.model.descriptor.IconEncoding
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

case object IconEncodingSerializer extends CustomSerializer[IconEncoding](format => ( {
  case JString(encoding) => encoding match {
    case "RAW" => IconEncoding.RAW
    case "Base64" => IconEncoding.Base64
  }
  case JNull => IconEncoding.RAW
}, {
  case encoding: IconEncoding =>
    JString(encoding.getClass.getSimpleName.replace("$", ""))
}))