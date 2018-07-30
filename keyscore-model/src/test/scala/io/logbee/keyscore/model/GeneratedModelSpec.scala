package io.logbee.keyscore.model

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class GeneratedModelSpec extends FreeSpec with  Matchers {

  private val exampleText = "The weather is cloudy with a current temperature of 11.5 C."

  "This test" - {
    "should check something" in new BufferAndStream {

      private val messageField = Field("message", TextValue(exampleText))

      private val record = Record(fields = List(
        messageField,
        Field("temperature", DecimalValue(11.5)),
        Field("report", NumberValue(1)),
        Field("timestamp", TimestampValue(4711L)
      )))

      val dataset = Dataset(None, List(record))

      dataset.writeTo(output)
      buffer.flip()

      val parsedDataset: Dataset = Dataset.parseFrom(input)

      parsedDataset shouldBe dataset

      parsedDataset.records.head.fields.foreach { field: Field =>
        field.value match {
          case text: TextValue =>
            text.value shouldBe exampleText
          case decimal: DecimalValue =>
            decimal.value shouldBe 11.5
          case number: NumberValue =>
            number.value shouldBe 1
          case timestamp: TimestampValue =>
            timestamp.value shouldBe 4711L
        }
      }
    }
  }
}
