package io.logbee.keyscore.model

import java.io.InputStreamReader

import io.logbee.keyscore.model.json4s._
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParameterDescriptorSerializerSpec extends AnyWordSpec with Matchers {

  implicit val formats = KeyscoreFormats.formats

  "A List of ParameterDescriptor" should {
    "be deserializable" in {
      // TODO: Implement a test to verify the serialization of ParameterDescriptor
      val parameterListReader = new InputStreamReader(getClass.getResourceAsStream("/parameterlist.example.json"))
//      val parameterList = read[List[ParameterDescriptor]](parameterListReader)

//      parameterList should have size 2
//      fail("Not Implemented!")
    }
  }
}
