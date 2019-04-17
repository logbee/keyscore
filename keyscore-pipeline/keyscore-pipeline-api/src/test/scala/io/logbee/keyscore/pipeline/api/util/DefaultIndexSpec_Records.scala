package io.logbee.keyscore.pipeline.api.util

import io.logbee.keyscore.model.data.{Field, Record, TextField, TimestampValue}
import io.logbee.keyscore.model.util.ToOption.T2OptionT
import io.logbee.keyscore.pipeline.api.util.Characteristic.whereAll
import io.logbee.keyscore.pipeline.api.util.DefaultDataIndex.FIELD_PRESENT
import io.logbee.keyscore.pipeline.api.util.DefaultDataIndex.Record.{byField, byPresentField, withField}
import org.scalatest.{FreeSpec, Matchers}

class DefaultIndexSpec_Records extends FreeSpec with Matchers {

  "A GroupingBookkeeper" - {

    val position = Name[String]("position")
    val timestamp = Name[String]("timestamp")

    val sampleA1 = Record(
      TextField("position", "42"),
      Field("timestamp", TimestampValue(1L, 0))
    )

    val sampleA2 = Record(
      TextField("position", "73"),
      Field("timestamp", TimestampValue(2L, 0))
    )

    val sampleA3 = Record(
      TextField("position", "42"),
    )

    "should return different groups per field" in {

      val grouping = DefaultDataIndex[Record]()

      Seq(sampleA1, sampleA2, sampleA3)
        .foreach(record => grouping.insert(record , byField("position"), byPresentField("timestamp")))

      grouping.select(whereAll(Attribute(position))).elements should contain only(sampleA1, sampleA2, sampleA3)
      grouping.select(whereAll(Attribute(position, "42"))).elements should contain only (sampleA1, sampleA3)
      grouping.select(whereAll(Attribute(position, "73"))).elements should contain only sampleA2
      grouping.select(whereAll(Attribute(position, "42"), Attribute(timestamp, FIELD_PRESENT))).elements should contain only sampleA1
    }

    "should return a sortable group" in {

      val grouping = DefaultDataIndex[Record]()

      Seq(sampleA1, sampleA2, sampleA3)
        .foreach(record => grouping.insert(record , byPresentField("timestamp")))

      val group = grouping.select(whereAll(Attribute(timestamp, FIELD_PRESENT)))

      group.sort(withField("timestamp"), Ascending).elements should contain inOrderOnly(sampleA2, sampleA1)
      group.sort(withField("timestamp"), Descending).elements should contain inOrderOnly(sampleA1, sampleA2)
    }
  }
}
