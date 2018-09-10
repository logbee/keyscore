package io.logbee.keyscore.model

import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ExpressionType.Grok
import io.logbee.keyscore.model.descriptor.FieldNameHint.{AbsentField, PresentField}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.json4s.KeyscoreFormats
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source


@RunWith(classOf[JUnitRunner])
class DescriptorSpec extends FreeSpec with Matchers {

  import io.logbee.keyscore.model.util.ToOption._
  import org.json4s.native.Serialization.{read, write, writePretty}

  implicit val formats = KeyscoreFormats.formats

  "A Dataset" - {
    "should" in {

      val EN = java.util.Locale.ENGLISH// Locale("en")
      val DE = java.util.Locale.GERMAN //Locale("de")

      val filterDisplayName = TextRef("displayName")
      val filterDescription = TextRef("description")
      val category = TextRef("filterCategory")

      val textParameterDisplayName = TextRef("foo")
      val textParameterDescription = TextRef("bar")

      val textParameter = TextParameterDescriptor("example.filter.simpleText", ParameterInfo(textParameterDisplayName, textParameterDescription), defaultValue = "Hello World", validator = StringValidator("Hello*", ExpressionType.Glob))

      val booleanParameterRef = ParameterRef("example.filter.theTruth")
      val booleanParameter = BooleanParameterDescriptor(booleanParameterRef, ParameterInfo(TextRef("booleanParameterDisplayName"), TextRef("booleanParameterDescription")), defaultValue = true)

      val patternParameter = ExpressionParameterDescriptor("example.filter.aGrokPattern", expressionType = Grok)

      val choiceParameter = ChoiceParameterDescriptor("example.filter.myChoice", min = 1, max = 1, choices = Seq(
        Choice("red"),
        Choice("green"),
        Choice("blue")
      ))

      val fieldParameter = FieldParameterDescriptor("example.filter.aConstField", defaultName = "message", hint = AbsentField, fieldValueType = FieldValueType.Text, mandatory = true)

      val descriptor = Descriptor(
        ref = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610",
        describes = FilterDescriptor(
          name = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic",
          displayName = filterDisplayName,
          description = filterDescription,
          categories = Seq(category),
          parameters = Seq(textParameter, booleanParameter, choiceParameter, fieldParameter,
            ParameterGroupDescriptor(condition = BooleanParameterCondition(booleanParameterRef, negate = true), parameters = Seq(
              patternParameter,
              FieldNameListParameterDescriptor("ff543cab-15bf-114a-47a1-ce1f065e5513",
                ParameterInfo("listParameterDisplayName", "listParameterDescription"),
                FieldNameParameterDescriptor(hint = PresentField, validator = StringValidator("^_.*", ExpressionType.RegEx)),
                min = 1, max = Int.MaxValue)
            ))
          ),
          icon = Icon.fromResource("/example-icon.svg")
        ),
        localization = Localization.fromJavaMapping(
          filterDisplayName -> Map(
            EN -> "Add Fields",
            DE -> "Feld Hinzufuegen"
          ),
          filterDescription -> Map(
            EN -> "Adds the specified fields.",
            DE -> "Fuegt die definierten Felder hinzu."
          ),
          category -> Map(
            EN -> "Source",
            DE -> "Quelle"
          ),
          textParameterDisplayName -> Map(
            EN -> "A Parameter",
            DE -> "Ein Parameter"
          ),
          textParameterDescription -> Map(
            EN -> "A simple text parameter as example.",
            DE -> "Ein einfacher Textparameter als Beispiel."
          )
        ) ++ Localization.fromResourceBundle("ExampleFilter", Locale.ENGLISH, Locale("de")))

      println(descriptor.toString())
      println(write(descriptor))
    }

    "should deserialize" in {
      val exmapleJson = Source.fromInputStream(getClass.getResourceAsStream("/example-filter-descriptor.json")).mkString

      val parsedDescriptor = read[Descriptor](exmapleJson)

      println(writePretty(parsedDescriptor))
    }
  }
}
