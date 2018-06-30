package io.logbee.keyscore.model

import java.util.UUID.randomUUID

import io.logbee.keyscore.model.NativeConversion._
import io.logbee.keyscore.model.NativeModel.{NativeDataset, NativeField, NativeRecord}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FreeSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class NativeConversionSpec extends FreeSpec with Matchers {

  val textField = TextField("message", "The weather is cloudy with a current temperature of: -11.5 °C")
  val numberField = NumberField("temperature", -11.5)
  val timestampField = TimestampField("timestamp", 1529948452746L)

  val fields = List(textField, numberField, timestampField)

  "A Field" - {

    fields.map((_, new BufferAndStream().tuple)).foreach({ case (field, (buffer, stream)) =>

      s"of kind ${field.kind}" - {

        "should be serializable to a NativeField and deserializable from a NativeField" in {

          val nativeTextField: NativeField = field

          nativeTextField.writeTo(stream)

          buffer.flip()

          val parsedTextField: Field[_] = NativeField.parseFrom(buffer)

          parsedTextField shouldBe field
        }
      }
    })
  }

  "A Record" - {

    "should be serializable to a NativeRecord and deserializable from a NativeRecord" in new BufferAndStream {

      val record = Record(randomUUID(), fields)

      val nativeRecord: NativeRecord = record

      nativeRecord.writeTo(stream)

      buffer.flip()

      val parsedRecord: Record = NativeRecord.parseFrom(buffer)

      parsedRecord shouldBe record
    }
  }

  "A Dataset" - {

    "should be serializable to a NativeDataset and deserializable from a NativeDataset" in new BufferAndStream {

      val metaData = MetaData(Map(
        Label[String]("someLabel") -> "sameValue"
      ))
      val dataset = Dataset(metaData,
        List(
          Record(randomUUID(), fields),
          Record(randomUUID(), TextField("message", "Is is a rainy day. Temperature: 5.8 °C"))
        )
      )

      val nativeDataset: NativeDataset = dataset

      nativeDataset.writeTo(stream)

      buffer.flip()

      val parsedDataset: Dataset = NativeDataset.parseFrom(buffer)

      parsedDataset shouldBe dataset
      //parsedDataset.metaData shouldBe dataset.metaData // TODO: Implement serialization/deserialization for MetaData
    }
  }
}