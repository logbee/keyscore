package io.logbee.keyscore.model.descriptor

import io.logbee.keyscore.model.data.Icon
import io.logbee.keyscore.model.data.IconEncoding.RAW
import io.logbee.keyscore.model.data.IconFormat.SVG
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

class IconLoaderSpec extends AnyFreeSpec with Matchers {

  val expectedData = Source.fromInputStream(getClass.getResourceAsStream("ExampleIcon.svg")).mkString

  "An IconLoader" - {

    "should load an icon from the passed class" in {

      val icon = Icon.fromClass(classOf[ExampleIcon])

      icon.data shouldBe expectedData
      icon.encoding shouldBe RAW
      icon.format shouldBe SVG
    }

    "should load an icon from passed resource path" in {

      val icon = Icon.fromResource("/io/logbee/keyscore/model/descriptor/ExampleIcon.svg")

      icon.data shouldBe expectedData
      icon.encoding shouldBe RAW
      icon.format shouldBe SVG
    }
  }
}

class ExampleIcon {

}
