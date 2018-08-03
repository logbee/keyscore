package io.logbee.keyscore.model

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class GeneratedModelSpec extends FreeSpec with  Matchers {

  private val exampleText = "The weather is cloudy with a current temperature of 11.5 C."
  private val exampleLabelText = "Hello World!"
  private val exampleLabelNumber = 42

  "This test" - {
    "should check something" in new BufferAndStream {

      val dataset = Dataset(
        MetaData(List(
          Label("foo", TextValue(exampleLabelText)),
          Label("bar", NumberValue(exampleLabelNumber))
        )),
        Record(
          Field("message", TextValue(exampleText)),
          TextField("message", exampleText),
          DecimalField("temperature",  11.5),
          Field("report", NumberValue(1)),
          Field("timestamp", TimestampValue(4711, 73)
        )
      ))

      dataset.writeTo(output)
      buffer.flip()

      val parsedDataset: Dataset = Dataset.parseFrom(input)

      parsedDataset.metadata.findLabel("foo").get.value shouldBe exampleLabelText
      parsedDataset.metadata.findLabel("bar").get.value shouldBe exampleLabelNumber

      parsedDataset.metadata.findLabel("fubar") shouldBe None
      parsedDataset.metadata.findTextLabel("bar") shouldBe None

      parsedDataset.records.head.fields.foreach { field: Field =>
        field.value match {
          case text: TextValue =>
            text.value shouldBe exampleText
          case decimal: DecimalValue =>
            decimal.value shouldBe 11.5
          case number: NumberValue =>
            number.value shouldBe 1
          case timestamp: TimestampValue =>
            timestamp.seconds shouldBe 4711
            timestamp.nanos shouldBe 73
          case _ => throw new IllegalStateException()
        }
      }
    }
  }
}
