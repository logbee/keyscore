package io.logbee.keyscore.model

import java.util.UUID

import io.logbee.keyscore.model.Entity.SeqOfComponents2MapOfComponent
import io.logbee.keyscore.model.EntityStore.ROOT_ANCESTOR
import io.logbee.keyscore.model.configuration.{NumberParameter, TextParameter}
import io.logbee.keyscore.model.descriptor.ParameterRef
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DefaultEntityStoreSpec extends AnyFreeSpec with Matchers with OptionValues with MockFactory {

  "A DefaultEntityStore" - {

    val exampleAUUID = "e05841b1-60f6-41ae-87ab-aa8b9dc6013f"
    val exampleBUUID = "a043b97e-f246-421a-8fc6-1fee3094e3e8"

    val exampleParameterSetComponent = ParameterSetComponent(Seq(TextParameter(ParameterRef("message"), "Hello World")))
    val exampleA = Entity(EntityRef(exampleAUUID), Seq(exampleParameterSetComponent))
    val exampleB = Entity(EntityRef(exampleBUUID))
    val aspectToDelete = Aspect.aspect(classOf[ParameterSetComponent])


    val modifiedParameterSetComponent = ParameterSetComponent(Seq(
      TextParameter(ParameterRef("message"), "Hello World"),
      TextParameter(ParameterRef("modified"), "02-11-2018"))
    )
    val modifiedExample = exampleA.update(
      _.components := Seq(modifiedParameterSetComponent)
    )

    val lastExampleParameterSetComponent = ParameterSetComponent(Seq(
      TextParameter(ParameterRef("message"), "Hello World"),
      TextParameter(ParameterRef("modified"), "02-11-2018"),
      TextParameter(ParameterRef("sample"), "The weather is cloudy."))
    )
    val lastExample = modifiedExample.update(
      _.components := Seq(lastExampleParameterSetComponent)
    )

    implicit val detector = new ChangeDetector[EntityRef] {
      override def detect(ancestor: Option[Entity], current: Option[Entity]): EntityRef = {
        current.getOrElse(Entity(EntityRef())).ref
      }
    }

    "with a committed entity" - {

      val store = new DefaultEntityStore()

      val exampleEntityRef = store.commit(exampleA)
      val modifiedExampleRef = store.commit(modifiedExample.update(
        _.ref.ancestor := exampleEntityRef.revision
      ))
      val lastExampleRef = store.commit(lastExample.update(
        _.ref.ancestor := modifiedExampleRef.revision
      ))

      "should return the committed entities" in {

        val entityA = store.find(exampleEntityRef)
        val entityB = store.find(modifiedExampleRef)

        entityA should be(Symbol("defined"))
        entityA.get.components.values should contain only exampleParameterSetComponent

        entityB should be(Symbol("defined"))
        entityB.get.components.values should contain only modifiedParameterSetComponent
      }

      "should return an empty list when there are not entities matching the given aspect" in  {
        val aspect = Aspect.aspect(classOf[DummyComponent])
        val entities = store.head(aspect)

        entities shouldBe empty
      }

      "should return the committed entities by aspect" in  {
        val id = UUID.randomUUID().toString
        val entity = Entity(EntityRef(id), components = Seq(ParameterSetComponent()))

        val aspect = Aspect.aspect(classOf[ParameterSetComponent])

        var entities = store.head(aspect)

        entities should have size 1

        store.commit(entity)

        entities = store.head(aspect)

        entities should have size 2
      }

      "should return all revisions of the specified entity" in {

        val entities = store.all(EntityRef(exampleAUUID))

        entities should have size 3
        entities.head.components.values should contain only (lastExample.components.values.toSeq:_*)
        entities(1).components.values should contain only (modifiedExample.components.values.toSeq:_*)
        entities(2).components.values should contain only (exampleA.components.values.toSeq:_*)
      }

      "should return an empty list if the specified entity is unknown" in {
        store.all(EntityRef("24d7da1b-00da-45fe-bc40-9105365468a1")) should have size 0
      }

      "should return the last committed entity if a the revision is not specified" in {

        val entity = store.head(EntityRef(exampleAUUID)).value
        entity.components.values should contain only lastExampleParameterSetComponent
      }

      "should return None if there is no Entity with the specified UUID" in {
        store.head(EntityRef("877e7c83-7b6d-4a43-acd1-6802ef00930f", exampleEntityRef.revision)) should be(Symbol("empty"))
        store.find(EntityRef("877e7c83-7b6d-4a43-acd1-6802ef00930f", exampleEntityRef.revision)) should be(Symbol("empty"))
      }

      "should return None if there is no Entity with the specified revision" in {
        store.find(EntityRef(exampleAUUID, "331a76f144d96cca5a31018c3055c20282ce75ac")) should be(Symbol("empty"))
        store.find(EntityRef(exampleAUUID)) should be(Symbol("empty"))
      }

      "should set the ancestor of first committed entities" in {
        exampleEntityRef.ancestor shouldBe ROOT_ANCESTOR
      }

      "should not commit an identical revision" in {

        val identicalEntity = lastExample.update(
          _.ref.ancestor := lastExampleRef.ancestor
        )

        val revisionsBefore = store.all(EntityRef(exampleAUUID))

        store.commit(identicalEntity)

        val revisionsAfter = store.all(EntityRef(exampleAUUID))

        revisionsAfter shouldBe revisionsBefore
      }

      "should throw a DivergedException if an Entity is committed with an unset ancestor" in {
        val entity = exampleA.update(
          _.ref.ancestor := "",
          _.components := Seq()
        )

        val exception = intercept[DivergedException] {
          store.commit(entity)
        }

        exception.base.components.values should contain only modifiedParameterSetComponent
        exception.theirs.components.values should contain only lastExampleParameterSetComponent
        exception.yours.components.values shouldBe empty

        exception.theirs.ref.ancestor shouldBe modifiedExampleRef.revision
        exception.yours.ref.ancestor shouldBe modifiedExampleRef.revision
      }

      "should throw a DivergedException when an Entity with the same ancestor was already committed" in {

        val entity = exampleA.update(
          _.ref.ancestor := modifiedExampleRef.ancestor,
          _.components := Seq()
        )

        val exception = intercept[DivergedException] {
          store.commit(entity)
        }

        exception.base.components.values should contain only modifiedParameterSetComponent
        exception.theirs.components.values should contain only lastExampleParameterSetComponent
        exception.yours.components.values shouldBe empty

        exception.theirs.ref.ancestor shouldBe modifiedExampleRef.revision
        exception.yours.ref.ancestor shouldBe modifiedExampleRef.revision
      }

      "should throw an UnknownEntityException if the specified entity does not exists to reset" in {
        val exception = intercept[UnknownEntityException] {
          store.reset(exampleEntityRef.update(
            _.uuid := "c6929d7f-c8e0-4b5e-a89e-fbd79f7c3ac3"
          ))
        }
      }

      "should throw an UnknownRevisionException if the specified revision does not exists to reset" in {
        val exception = intercept[UnknownRevisionException] {
          store.reset(exampleEntityRef.update(
            _.revision := "331a76f144d96cca5a31018c3055c20282ce75ac"
          ))
        }
      }
    }

    "(when the last revision of a entity gets reverted)" - {

      val entityStore = new DefaultEntityStore()

      val exampleRef = entityStore.commit(exampleA)
      val modifiedExampleRef = entityStore.commit(modifiedExample.update(
        _.ref.ancestor := exampleRef.revision
      ))
      val lastExampleRef = entityStore.commit(lastExample.update(
        _.ref.ancestor := modifiedExampleRef.revision
      ))

      val revertedRef = entityStore.revert(lastExampleRef)

      "should return a ref with a new revision and the passed ref as ancestor" in {

        revertedRef should not be null

        revertedRef.revision should not be oneOf(
          exampleRef.revision,
          modifiedExampleRef.revision,
          lastExampleRef.revision
        )

        revertedRef.ancestor shouldBe lastExampleRef.revision
      }

      "should return the previous entity" in {

        val entity = entityStore.head(EntityRef(exampleAUUID)).value
        entity.components.values should contain only modifiedParameterSetComponent
      }
    }

    "(when a revision of a entity gets reverted)" - {

      val entityStore = new DefaultEntityStore()

      val exampleRef = entityStore.commit(exampleA)
      val modifiedExampleRef = entityStore.commit(modifiedExample.update(
        _.ref.ancestor := exampleRef.revision
      ))
      val lastExampleRef = entityStore.commit(lastExample.update(
        _.ref.ancestor := modifiedExampleRef.revision
      ))

      "should throw an DivergedException" in {

        val exception = intercept[DivergedException] {
          entityStore.revert(modifiedExampleRef)
        }

        exception.base.components.values should contain only exampleParameterSetComponent
        exception.theirs.components.values should contain only lastExampleParameterSetComponent
        exception.yours.components.values should contain only modifiedParameterSetComponent
      }

      "should throw an DivergedException where base is null if the reverted entity is the root" in {

        val exception = intercept[DivergedException] {
          entityStore.revert(exampleRef)
        }

        exception.base shouldBe null
        exception.theirs.components.values should contain only lastExampleParameterSetComponent
        exception.yours.components.values should contain only exampleParameterSetComponent
      }

      "(when a entity gets reset to a specific revision)" - {

        val store = new DefaultEntityStore()

        val exampleRef = store.commit(exampleA)
        val modifiedExampleRef = store.commit(modifiedExample.update(
          _.ref.ancestor := exampleRef.revision
        ))
        store.commit(lastExample.update(
          _.ref.ancestor := modifiedExampleRef.revision
        ))

        val ref = store.reset(exampleRef)

        "should return the entity with the specified revision as last" in {

          val entity = store.head(EntityRef(exampleAUUID)).value
          ref shouldBe exampleRef
          entity.ref shouldBe ref
          entity.components.values should contain only (exampleA.components.values.toSeq:_*)
        }
      }

      "(when the revisions of a entity are removed)" - {

        val store = new DefaultEntityStore()

        val exampleRef = store.commit(exampleA)
        val modifiedExampleRef = store.commit(modifiedExample.update(
          _.ref.ancestor := exampleRef.revision
        ))

        store.delete(EntityRef(exampleAUUID))

        "should return None" in {
          store.head(EntityRef(exampleAUUID)) shouldBe None
        }

        "should return None for any revision" in {
          store.find(exampleRef) shouldBe None
          store.find(modifiedExampleRef) shouldBe None
        }

        "should return an empty Seq" in {
          store.all(EntityRef(exampleAUUID)) shouldBe empty
        }
      }

      "with several committed Entities" - {

        val store = new DefaultEntityStore()

        val fieldParameter = TextParameter(ParameterRef("fieldName"), "message")
        val hostParameter = TextParameter(ParameterRef("hostname"), "example.com")
        val portParameter = NumberParameter(ParameterRef("port"), 9092)

        val exampleA = Entity(EntityRef("1452dc63-68db-404e-a79d-6143b3526809"), components = Seq(
          ParameterSetComponent(Seq(
            fieldParameter
          ))
        ))

        val exampleB = Entity(EntityRef("a9080e02-dd5f-48d8-8995-51af63a8eedc"), components = Seq(
          ParameterSetComponent(Seq(
            hostParameter
          ))
        ))

        val exampleARef = store.commit(exampleA)
        val exampleB1Ref = store.commit(exampleB.update(
          _.ref.ancestor := exampleARef.revision
        ))
        val exampleB2Ref = store.commit(exampleB.update(
          _.ref.ancestor := exampleB1Ref.revision,
          _.components := Seq(ParameterSetComponent(Seq(portParameter, fieldParameter, hostParameter)))
        ))

        "should return the head revisions of all Entities" in {

          val entities = store.head()
          entities should have size 2
        }
      }

      "should throw an UnknownAncestorException if the specified ancestor is not a known revision" in {

        val store = new DefaultEntityStore()

        store.commit(exampleA)

        val exception = intercept[UnknownAncestorException] {
          store.commit(exampleA.update(
            _.ref.ancestor := "1452dc63-68db-404e-a79d-6143b3526809"
          ))
        }

        exception.ref.uuid shouldBe exampleAUUID
        exception.ref.ancestor shouldBe Hash("1452dc63-68db-404e-a79d-6143b3526809")
      }

      "should delete all Entities by Aspect" in {
        val store = new DefaultEntityStore()

        val exampleAEntityRef = store.commit(exampleA)
        val exampleBEntityRef = store.commit(exampleB)

        val modifiedExampleRef = store.commit(modifiedExample.update(
          _.ref.ancestor := exampleAEntityRef.revision
        ))

        store.deleteAll(aspectToDelete)

        store.find(exampleAEntityRef) shouldBe None
        store.find(modifiedExampleRef) shouldBe None
        store.find(exampleBEntityRef) shouldBe defined
      }
    }
  }
}
