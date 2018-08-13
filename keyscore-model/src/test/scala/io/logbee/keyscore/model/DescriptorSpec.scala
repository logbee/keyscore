package io.logbee.keyscore.model

import org.scalatest.{FreeSpec, Matchers}
import scalapb.json4s.JsonFormat


class DescriptorSpec extends FreeSpec with Matchers {

  import io.logbee.keyscore.model.ToOption._
  import org.json4s.jackson.JsonMethods._

  "A Dataset" - {
    "should" in {

      val EN = Locale("en")
      val DE = Locale("de/DE")

      val filterDisplayName = TextRef("10d2b680-17c8-47b5-9ce6-2f3facf6e764")
      val filterDescription = TextRef("c46ce8dc-d55c-443c-bc97-118ca4911592")
      val category = TextRef("aa5de1cd-1122-758f-97fa-228ca8911378")

      val parameterARef = ParameterRef("37024d8b-4aec-4b3e-8074-21ef065e5ee2")
      val parameterADisplayName = TextRef("c36c2134-bc89-4306-85bf-ec02180717fc")
      val parameterADescription = TextRef("49cfa908-bd3a-417f-bdd8-850e3505c312")

      val descriptor = Descriptor(
        id = "1a6e5fd0-a21b-4056-8a4a-399e3b4e7610",
        describe = FilterDescriptor(
          name = "io.logbee.keyscore.agent.pipeline.contrib.filter.AddFieldsFilterLogic",
          displayName = filterDisplayName,
          description = filterDescription,
          category = category,
          parameters = Map(
            parameterARef -> TextParameterDescriptor(parameterADisplayName, parameterADescription)
          )
        ),
        localisation = Map(
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
          )
        )
      )

      println(descriptor)

      val json = JsonFormat.toJson(descriptor)
      println(pretty(json))
    }
  }
}
