package io.logbee.keyscore.model.configuration

import io.logbee.keyscore.model.configuration.ConfigurationRepository.{DivergedException, ROOT_ANCESTOR, UnknownConfigurationException, UnknownRevisionException}
import io.logbee.keyscore.model.descriptor.ParameterRef
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers, OptionValues}

@RunWith(classOf[JUnitRunner])
class ConfigurationRepositorySpec extends FreeSpec with Matchers with OptionValues {

  "A ConfigurationRepository" - {

    val exampleConfigurationUUID = "e05841b1-60f6-41ae-87ab-aa8b9dc6013f"
    val exampleConfiguration = Configuration(ConfigurationRef(exampleConfigurationUUID), parameters = Seq(TextParameter(ParameterRef("message"), "Hello World")))
    val modifiedExampleConfiguration = exampleConfiguration.update(
      _.parameters :+= TextParameter(ParameterRef("modified"), "02-11-2018")
    )
    val lastExampleConfiguration = modifiedExampleConfiguration.update(
      _.parameters :+= TextParameter(ParameterRef("sample"), "The weather is cloudy.")
    )

    "with a committed configuration" - {

      val repository = new ConfigurationRepository()

      val exampleConfigurationRef = repository.commit(exampleConfiguration)
      val modifiedExampleConfigurationRef = repository.commit(modifiedExampleConfiguration)
      val lastExampleConfigurationRef = repository.commit(lastExampleConfiguration)

      "should return the committed configurations" in {

        val configA = repository.get(exampleConfigurationRef)
        val configB = repository.get(modifiedExampleConfigurationRef)

        configA should be('defined)
        configA.get.findTextValue(ParameterRef("message")).value shouldBe "Hello World"
        configA.get.findTextValue(ParameterRef("date")) should be('empty)

        configB should be('defined)
        configB.get.findTextValue(ParameterRef("message")).value shouldBe "Hello World"
        configB.get.findTextValue(ParameterRef("modified")).value shouldBe "02-11-2018"
      }

      "should return all revisions of the specified configuration" in {
        repository.all(ConfigurationRef(exampleConfigurationUUID)) should have size 3
      }

      "should return an empty list if the specified configuration is unknown" in {
        repository.all(ConfigurationRef("24d7da1b-00da-45fe-bc40-9105365468a1")) should have size 0
      }

      "should return the last committed configuration if a the revision is not specified" in {

        val config = repository.head(ConfigurationRef(exampleConfigurationUUID)).value
        config.parameters shouldBe lastExampleConfiguration.parameters
      }

      "should return None if there is no Configuration with the specified UUID" in {
        repository.head(ConfigurationRef("877e7c83-7b6d-4a43-acd1-6802ef00930f", exampleConfigurationRef.revision)) should be('empty)
        repository.get(ConfigurationRef("877e7c83-7b6d-4a43-acd1-6802ef00930f", exampleConfigurationRef.revision)) should be('empty)
      }

      "should return None if there is no Configurtaion with the specified revision" in {
        repository.get(ConfigurationRef(exampleConfigurationUUID, "331a76f144d96cca5a31018c3055c20282ce75ac")) should be('empty)
        repository.get(ConfigurationRef(exampleConfigurationUUID)) should be('empty)
      }

      "should set the ancestor of committed configurations" in {
        exampleConfigurationRef.ancestor shouldBe ROOT_ANCESTOR
        modifiedExampleConfigurationRef.ancestor shouldBe exampleConfigurationRef.revision
        lastExampleConfigurationRef.ancestor shouldBe modifiedExampleConfigurationRef.revision
      }

      "should throw a DivergedException when a Configuration with the same ancestor was already committed" in {

        val configuration = exampleConfiguration.update(
          _.ref.ancestor := modifiedExampleConfigurationRef.ancestor,
          _.parameters :+= NumberParameter(ParameterRef("count"), 42)
        )

        val exception = intercept[DivergedException] {
          repository.commit(configuration)
        }

        exception.base.parameters shouldBe exampleConfiguration.parameters
        exception.theirs.parameters shouldBe lastExampleConfiguration.parameters
        exception.yours.parameters shouldBe configuration.parameters
      }

      "should throw an UnknownConfigurationException if the specified configuration does not exists to reset" in {
        val exception = intercept[UnknownConfigurationException] {
          repository.reset(exampleConfigurationRef.update(
            _.uuid := "c6929d7f-c8e0-4b5e-a89e-fbd79f7c3ac3"
          ))
        }
      }

      "should throw an UnknownRevisionException if the specified revision does not exists to reset" in {
        val exception = intercept[UnknownRevisionException] {
          repository.reset(exampleConfigurationRef.update(
            _.revision := "331a76f144d96cca5a31018c3055c20282ce75ac"
          ))
        }
      }
    }

    "(when the last revision of a configuration gets reverted)" - {

      val repository = new ConfigurationRepository()

      val exampleConfigurationRef = repository.commit(exampleConfiguration)
      val modifiedExampleConfigurationRef = repository.commit(modifiedExampleConfiguration)
      val lastExampleConfigurationRef = repository.commit(lastExampleConfiguration)

      val revertedRef = repository.revert(lastExampleConfigurationRef)

      "should return a ref with a new revision and the passed ref as ancestor" in {

        revertedRef should not be null

        revertedRef.revision should not be oneOf(
          exampleConfigurationRef.revision,
          modifiedExampleConfigurationRef.revision,
          lastExampleConfigurationRef.revision
        )

        revertedRef.ancestor shouldBe lastExampleConfigurationRef.revision
      }

      "should return the previous configuration" in {

        val config = repository.head(ConfigurationRef(exampleConfigurationUUID)).value
        config.parameters shouldBe modifiedExampleConfiguration.parameters
      }
    }

    "(when a revision of a configuration gets reverted)" - {

      val repository = new ConfigurationRepository()

      val exampleConfigurationRef = repository.commit(exampleConfiguration)
      val modifiedExampleConfigurationRef = repository.commit(modifiedExampleConfiguration)
      val lastExampleConfigurationRef = repository.commit(lastExampleConfiguration)

      "should throw an DivergedException" in {

        val exception = intercept[DivergedException] {
          repository.revert(modifiedExampleConfigurationRef)
        }

        exception.base.parameters shouldBe exampleConfiguration.parameters
        exception.theirs.parameters shouldBe lastExampleConfiguration.parameters
        exception.yours.parameters shouldBe modifiedExampleConfiguration.parameters
      }

      "should throw an DivergedException where base is null if the reverted configuration is the root" in {

        val exception = intercept[DivergedException] {
          repository.revert(exampleConfigurationRef)
        }

        exception.base shouldBe null
        exception.theirs.parameters shouldBe lastExampleConfiguration.parameters
        exception.yours.parameters shouldBe exampleConfiguration.parameters
      }
    }

    "(when a configuration gets reset to a specific revision)" - {

      val repository = new ConfigurationRepository()

      val exampleConfigurationRef = repository.commit(exampleConfiguration)

      repository.commit(modifiedExampleConfiguration)
      repository.commit(lastExampleConfiguration)

      repository.reset(exampleConfigurationRef)

      "should return the configuration with the specified revision as last" in {

        val config = repository.head(ConfigurationRef(exampleConfigurationUUID)).value
        config.parameters shouldBe exampleConfiguration.parameters
      }
    }

    "(when the revisions of a configuration are removed)" - {

      val repository = new ConfigurationRepository()

      val exampleConfigurationRef = repository.commit(exampleConfiguration)
      val modifiedExampleConfigurationRef = repository.commit(modifiedExampleConfiguration)

      repository.remove(ConfigurationRef(exampleConfigurationUUID))

      "should return None" in {
        repository.head(ConfigurationRef(exampleConfigurationUUID)) shouldBe None
      }

      "should return None for any revision" in {
        repository.get(exampleConfigurationRef) shouldBe None
        repository.get(modifiedExampleConfigurationRef) shouldBe None
      }

      "should return an empty Seq" in {
        repository.all(ConfigurationRef(exampleConfigurationUUID)) shouldBe empty
      }
    }
  }
}
