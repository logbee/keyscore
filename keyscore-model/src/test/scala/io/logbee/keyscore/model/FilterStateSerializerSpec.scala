package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.filter.{FilterState, Running}
import io.logbee.keyscore.model.json4s.{FilterConfigTypeHints, FilterStatusSerializer, HealthSerializer}
import org.json4s.ShortTypeHints
import org.json4s.ext.JavaTypesSerializers
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class FilterStateSerializerSpec extends WordSpec with Matchers{

  implicit val formats = Serialization.formats(ShortTypeHints(classOf[TextField] :: classOf[NumberField] :: classOf[TimestampField] :: Nil) + FilterConfigTypeHints) ++ JavaTypesSerializers.all ++ List(HealthSerializer, FilterStatusSerializer)

  "A FilterState" should  {
    "should be serializable" in {
      val filterState =  FilterState(UUID.randomUUID(), Green, 0, 0, Running)
      val filterStateToJson = write[FilterState](filterState)
      println(filterStateToJson)

      val parsedFilterStatus = read[FilterState](filterStateToJson)
      parsedFilterStatus.status shouldBe Running
      parsedFilterStatus.health shouldBe Green
    }

  }
}
