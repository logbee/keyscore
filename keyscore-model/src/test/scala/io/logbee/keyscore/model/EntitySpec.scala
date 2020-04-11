package io.logbee.keyscore.model

import io.logbee.keyscore.model.Entity.SeqOfComponents2MapOfComponent
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import org.json4s.FullTypeHints
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class EntitySpec extends AnyFreeSpec with Matchers {

  val hints = FullTypeHints(List(
    classOf[ParameterSetComponent],
    classOf[ConfigurationComponent],
    classOf[MetaDataComponent],
  ))

  implicit val formats = KeyscoreFormats.formats + hints

  import Aspect.aspect

  "An Entity" - {

    "should return the component of the specified type" in {

      val component = ParameterSetComponent()
      val entity = Entity(EntityRef(), components = Seq(component))

      entity.get[ParameterSetComponent] shouldBe component
    }

    "should throw NoSuchElementException" in {

      val entity = Entity(EntityRef())

      val exception = intercept[NoSuchElementException] {
        entity.get[ParameterSetComponent]
      }

      exception.getMessage should include (classOf[ParameterSetComponent].getName)
    }

    "should return whether a certain aspect matches" in  {

      val entity = Entity(EntityRef(), Seq(DummyComponent()))

      entity.matches(aspect(classOf[DummyComponent])) shouldBe true
      entity.matches(aspect(classOf[ParameterSetComponent])) shouldBe false
    }
  }
}