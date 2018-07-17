package io.logbee.keyscore.model

import java.io.InputStreamReader

import io.logbee.keyscore.model.filter._
import io.logbee.keyscore.model.json4s._
import org.json4s.native.Serialization.read
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class ParameterDescriptorSerializerSpec extends WordSpec with Matchers {

  implicit val formats = KeyscoreFormats.formats

  "A List of ParameterDescriptor" should {
    "be deserializable" in {
      val parameterListReader = new InputStreamReader(getClass.getResourceAsStream("/parameterlist.example.json"))
      val parameterList = read[List[ParameterDescriptor]](parameterListReader)

      parameterList should have size 2
    }
  }
}
