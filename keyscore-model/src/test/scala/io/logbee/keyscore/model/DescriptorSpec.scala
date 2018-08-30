package io.logbee.keyscore.model

import io.logbee.keyscore.model.data._
import io.logbee.keyscore.model.descriptor.ExpressionType.{Glob, Grok, JSONPath, RegEx}
import io.logbee.keyscore.model.descriptor.FieldNameHint.{AbsentField, AnyField, PresentField}
import io.logbee.keyscore.model.descriptor._
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import org.json4s.JsonAST.{JNull, JString}
import org.json4s.native.Serialization
import org.json4s.{CustomKeySerializer, CustomSerializer, FullTypeHints}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source


@RunWith(classOf[JUnitRunner])
class DescriptorSpec extends FreeSpec with Matchers {

  import io.logbee.keyscore.model.ToOption._
  import org.json4s.native.Serialization.{read, write, writePretty}

  implicit val formats = Serialization.formats(FullTypeHints(List(
    classOf[Descriptor],
    classOf[FilterDescriptor],
    classOf[ParameterDescriptor],
    classOf[ParameterGroupCondition]
  ))) + TextRefKeySerializer + LocaleKeySerializer + PatternTypeSerializer + FieldNameHintSerializer + FieldValueTypeSerializer

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
          )
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
        ) ++ Localization.fromResourceBundle("ExampleFilter", java.util.Locale.ENGLISH, Locale("de")))

      println(write(descriptor))
    }

    "should deserialize" in {
      val exmapleJson = Source.fromInputStream(getClass.getResourceAsStream("/example-filter-descriptor.json")).mkString

      val parsedDescriptor = read[Descriptor](exmapleJson)

      println(writePretty(parsedDescriptor))
    }
  }

  object TextRefKeySerializer extends CustomKeySerializer[TextRef](format => ({
    case id: String => TextRef(id)
  }, {
    case ref: TextRef => ref.id
  }))

  object LocaleKeySerializer extends CustomKeySerializer[Locale](format => ({
    case locale: String => Locale(locale)
  }, {
    case locale: Locale => Locale.localeToString(locale)
  }))

  object ParameterRefKeySerializer extends CustomKeySerializer[ParameterRef](format => ({
    case id: String => ParameterRef(id)
  }, {
    case ref: ParameterRef => ref.id
  }))

  case object ParameterRefSerializer extends CustomSerializer[ParameterRef](format => ( {
    case JString(ref) => ParameterRef(ref)
    case JNull => null
  }, {
    case ref: ParameterRef =>
      JString(ref.id)
  }))

  case object PatternTypeSerializer extends CustomSerializer[ExpressionType](format => ( {
    case JString(patternType) => patternType match {
      case "RegEx" => RegEx
      case "Grok" => Grok
      case "Glob" => Glob
      case "JSONPath" => JSONPath
    }
    case JNull => RegEx
  }, {
    case expressionType: ExpressionType => JString(expressionType.getClass.getSimpleName.replace("$", ""))
  }))

  case object FieldNameHintSerializer extends CustomSerializer[FieldNameHint](format => ( {
    case JString(hint) => hint match {
      case "AnyField" => AnyField
      case "PresentField" => PresentField
      case "AbsentField" => AbsentField
    }
    case JNull => AnyField
  }, {
    case hint: FieldNameHint =>
      JString(hint.getClass.getSimpleName.replace("$", ""))
  }))

  case object FieldValueTypeSerializer extends CustomSerializer[FieldValueType](format => ({
    case JString(fieldValueType) => fieldValueType match {
      case "Unkown" => FieldValueType.Unkown
      case "Boolean" => FieldValueType.Boolean
      case "Number" => FieldValueType.Number
      case "Decimal" => FieldValueType.Decimal
      case "Text" => FieldValueType.Text
      case "Timestamp" => FieldValueType.Timestamp
      case "Duration" => FieldValueType.Duration
    }
    case JNull => FieldValueType.Unkown
  }, {
    case fieldValueType: FieldValueType =>
      JString(fieldValueType.getClass.getSimpleName.replace("$", ""))
  }))
}
