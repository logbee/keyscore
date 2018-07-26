package io.logbee.keyscore.model

import io.logbee.keyscore.model.Field.ValueTypeOneof.{Decimal, Number, Text, Timestamp}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class GeneratedModelSpec extends FreeSpec with  Matchers {

  private val exampleText = "The weather is cloudy with a current temperature of 11.5 C."

  "This test" - {
    "should check something" in new BufferAndStream {
      val dataset = Dataset(None, List(
        Record(fields = List(
          Field("message", Text(TextValue(exampleText))),
          Field("temperature").withDecimal(DecimalValue(11.5)),
          Field("report").withNumber(NumberValue(1)),
          Field("timestamp").withTimestamp(TimestampValue(4711L))
        ))
      ))

      dataset.writeTo(output)
      buffer.flip()

      val parsedDataset: Dataset = Dataset.parseFrom(input)

      parsedDataset shouldBe dataset

      parsedDataset.records.head.fields.map(_.valueType).foreach {
        case textField: Text =>
          textField.isText shouldBe true
          textField.value.text shouldBe exampleText
        case decimalField: Decimal =>
          decimalField.isDecimal shouldBe true
          decimalField.value.decimal shouldBe 11.5
        case numberField: Number =>
          numberField.isNumber shouldBe true
          numberField.value.number shouldBe 1
        case timestampField: Timestamp =>
          timestampField.isTimestamp shouldBe true
          timestampField.value.timestamp shouldBe 4711L
      }
    }
  }
}
