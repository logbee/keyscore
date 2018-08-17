package io.logbee.keyscore.model

import com.google.protobuf.`type`.Field.Cardinality
import io.logbee.keyscore.model.FieldNameHint.{AbsentField, AnyField, PresentField}
import io.logbee.keyscore.model.ParameterDescriptorMessage.SealedValue.ChoiceParameter
import io.logbee.keyscore.model.PatternType.{Glob, Grok, RegEx}
import io.logbee.keyscore.model.configuration.{Configuration, TextParameterConfiguration}
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import org.json4s.JsonAST.{JNull, JString}
import org.json4s.native.Serialization
import org.json4s.{CustomKeySerializer, CustomSerializer, FullTypeHints, NoTypeHints, ShortTypeHints}
import org.scalatest.{FreeSpec, Matchers}

import scala.io.Source


class DescriptorSpec extends FreeSpec with Matchers {

  import io.logbee.keyscore.model.ToOption._
  import org.json4s.native.Serialization.{write, read, writePretty}

  implicit val formats = Serialization.formats(FullTypeHints(List(
    classOf[Descriptor],
    classOf[FilterDescriptor],
    classOf[ParameterDescriptor],
    classOf[ConditionalParameterCondition]
  ))) + TextRefKeySerializer + LocaleKeySerializer + PatternTypeSerializer + FieldNameHintSerializer

  "A Dataset" - {
    "should" in {

      val EN = Locale("en", "US")
      val DE = Locale("de/DE")

      val filterDisplayName = TextRef("displayName")
      val filterDescription = TextRef("description")
      val category = TextRef("aa5de1cd-1122-758f-97fa-228ca8911378")

      val parameterARef = ParameterRef("37024d8b-4aec-4b3e-8074-21ef065e5ee2")
      val parameterADisplayName = TextRef("parameterADisplayName")
      val parameterADescription = TextRef("parameterADescription")

//      val parameterBRef = ParameterRef("ff543cab-15bf-114a-47a1-ce1f065e5513")
      val parameterBDisplayName = TextRef("parameterBDisplayName")
      val parameterBDescription = TextRef("parameterBDescription")

      val parameterCRef = ParameterRef("b7cc9c84-ae6e-4ea3-bbff-f8d62af4caed")
//      val parameterDRef = ParameterRef("5f28c6dd-f88f-4530-afd1-c8b946bc5406")

      val descriptor = Descriptor(
        id = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610",
        describe = FilterDescriptor(
          name = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic",
          displayName = filterDisplayName,
          description = filterDescription,
          category = category,
          parameters = Seq(
            TextParameterDescriptor(parameterARef, ParameterInfo(parameterADisplayName, parameterADescription), defaultValue = "Hello World", validator = StringValidator("Hello*", PatternType.Glob)),
            BooleanParameterDescriptor(parameterCRef, ParameterInfo(TextRef("parameterDDisplayName"), TextRef("parameterDDescription")), defaultValue = true),
            ConditionalParameterDescriptor(condition = BooleanParameterCondition(parameterCRef, negate = true), parameters = Seq(
              PatternParameterDescriptor("98276284-a309-4f21-a0d8-50ce20e3376a", patternType = Grok),
              ListParameterDescriptor("ff543cab-15bf-114a-47a1-ce1f065e5513",
                ParameterInfo(parameterBDisplayName, parameterBDescription),
                FieldNameParameterDescriptor(hint = PresentField, validator = StringValidator("^_.*", PatternType.RegEx)),
                min = 1, max = Int.MaxValue)
            )),
            ChoiceParameterDescriptor("e84ad685-b7ad-421e-80b4-d12e5ca2b4ff", min = 1, max = 1, choices = Seq(
              Choice("red"),
              Choice("green"),
              Choice("blue")
            ))
          )
        ),
        localization = Localization(Set(EN, DE), Map(
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
          parameterADisplayName -> Map(
            EN -> "A Parameter",
            DE -> "Ein Parameter"
          ),
          parameterADescription -> Map(
            EN -> "A simple text parameter as example.",
            DE -> "Ein einfacher Textparameter als Beispiel."
          ),
          parameterBDisplayName -> Map(
            EN -> "A Parameter",
            DE -> "Ein Parameter"
          ),
          parameterBDescription -> Map(
            EN -> "A simple text parameter as example.",
            DE -> "Ein einfacher Textparameter als Beispiel."
          )
        )
      ))

      val config = Configuration()
      val textConfig = TextParameterConfiguration()

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

  case object PatternTypeSerializer extends CustomSerializer[PatternType](format => ( {
    case JString(patternType) => patternType match {
      case "RegEx" => RegEx
      case "Grok" => Grok
      case "Glob" => Glob
    }
    case JNull => RegEx
  }, {
    case patternType: PatternType => JString(patternType.getClass.getSimpleName.replace("$", ""))
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
}
