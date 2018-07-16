package io.logbee.keyscore.model

import java.io.InputStreamReader

import io.logbee.keyscore.model.filter.ParameterDescriptor
import io.logbee.keyscore.model.json4s.{FilterConfigTypeHints, FilterStatusSerializer, HealthSerializer, ParameterDescriptorTypeHints}
import org.json4s.ShortTypeHints
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class ParameterDescriptorSerializerSpec extends WordSpec with Matchers {
  implicit val formats = Serialization.formats(
    ShortTypeHints(
      classOf[TextField] ::
        classOf[NumberField] ::
        classOf[TimestampField] :: Nil)
      + FilterConfigTypeHints + ParameterDescriptorTypeHints) ++ JavaTypesSerializers.all ++ List(HealthSerializer, FilterStatusSerializer)

  "A List of ParameterDescriptor" should {
    "be deserializable" in {
      val parameterListReader = new InputStreamReader(getClass.getResourceAsStream("/parameterlist.example.json"))
      val parameterList = read[List[ParameterDescriptor]](parameterListReader)

      parameterList should have size 2
    }
  }
}
