package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.data.Health.Green
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.pipeline.{FilterState, Running}
import org.json4s.native.Serialization.{read, write}
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FilterStateSerializerSpec extends AnyWordSpec with Matchers {

  implicit val formats = KeyscoreFormats.formats

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
