package io.logbee.keyscore.test.util

import com.consol.citrus.message.Message
import org.json4s.Formats
import org.json4s.native.Serialization.read

object CitrusUtils {

  def readPayload[T](message: Message)(implicit format: Formats, mf: Manifest[T]): T = {
    read[T](message.getPayload().asInstanceOf[String])
  }
}
