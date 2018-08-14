package io.logbee.keyscore.model

import io.logbee.keyscore.model.ConditionalParameterConditionMessage.SealedValue.BooleanParameterCondition
import io.logbee.keyscore.model.FieldNameParameterDescriptor.FieldNameHint.PresentField
import io.logbee.keyscore.model.localization.{Locale, Localization, TextRef}
import org.scalatest.{FreeSpec, Matchers}
import scalapb.json4s.JsonFormat


class DescriptorSpec extends FreeSpec with Matchers {

  import io.logbee.keyscore.model.ToOption._
  import org.json4s.jackson.JsonMethods._

  "A Dataset" - {
    "should" in {

      val EN = Locale("en", "US")
      val DE = Locale("de/DE")

      val filterDisplayName = TextRef("displayName")
      val filterDescription = TextRef("description")
      val category = TextRef("aa5de1cd-1122-758f-97fa-228ca8911378")

//      val parameterARef = ParameterRef("37024d8b-4aec-4b3e-8074-21ef065e5ee2")
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
            TextParameterDescriptor("37024d8b-4aec-4b3e-8074-21ef065e5ee2", ParameterInfo(parameterADisplayName, parameterADescription), "Hello World", ".*"),
            BooleanParameterDescriptor(parameterCRef, ParameterInfo(TextRef("parameterDDisplayName"), TextRef("parameterDDescription")), defaultValue = true),
            ConditionalParameterDescriptor(condition = BooleanParameterCondition(parameterCRef, negate = true), parameters = Seq(
              ListParameterDescriptor("ff543cab-15bf-114a-47a1-ce1f065e5513", ParameterInfo(parameterBDisplayName, parameterBDescription), FieldNameParameterDescriptor(hint = PresentField))
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

//      println(descriptor)

      val json = JsonFormat.toJson(descriptor)
      println(pretty(json))
    }
  }
}
