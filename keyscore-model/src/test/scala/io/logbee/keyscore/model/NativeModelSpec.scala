package io.logbee.keyscore.model

import java.io.{File, FileInputStream, FileOutputStream, OutputStream}
import java.nio.ByteBuffer

import io.logbee.keyscore.model.Field.fieldFromNative
import io.logbee.keyscore.model.NativeModel._
import org.scalatest.{FreeSpec, Matchers, WordSpec}

class NativeModelSpec extends FreeSpec with Matchers {

  "A NativeField" - {

    val nativeField = NativeField.newBuilder.setName("message").setKind("text").setText("Hello World!").build()

    "when serialized" - {

      val buffer = ByteBuffer.allocate(10 * 1024)
      nativeField.writeTo(newOutputStream(buffer))
      buffer.flip()

      "should be parsable from a ByteBuffer" in {

        val parsedTextField: TextField = NativeField.parseFrom(buffer)

        parsedTextField.name shouldBe "message"
        parsedTextField.kind shouldBe "text"
        parsedTextField.value shouldBe "Hello World!"
      }
    }
  }

  "A TextField" - {

    val textField = TextField("message", "Hello World!")

    "when converted to a NativeField" - {

      val nativeField: NativeField = textField
      val buffer = ByteBuffer.allocate(10 * 1024)

      "should be serializable into a ByteBuffer" in {

        nativeField.writeTo(newOutputStream(buffer))
      }
    }
  }

//  "A Record" should {
//
//    val record = NativeRecord.newBuilder()
//      .addField(NativeField.newBuilder.setName("message").setText("Its a test!").build())
//      .addField(NativeField.newBuilder.setName("greeting").setText("Hello World").build())
//      .addField(NativeField.newBuilder.setName("temperature").setNumber(42.73).build())
//      .addField(NativeField.newBuilder.setName("timestamp").setTimestamp(1529948452746L).build())
//      .build()
//
//    val dataset = NativeDataset.newBuilder()
//      .setMetadata(NativeMetaData.newBuilder
//        .addLabel(NativeLabel.newBuilder.setName("LabelA").setValue("42"))
//        .addLabel(NativeLabel.newBuilder.setName("LabelB").setValue("A Test")))
//      .addRecord(record)
//      .build()
//
//    val textField: NativeField = TextField("FieldA", "Hello World")
//    val numberField: NativeField = NumberField("FieldB", 42.73)
//    val timestampField: NativeField = TimestampField("FieldC", 1529948452746L)
//
//    "foo" in {
//
//      val buffer = ByteBuffer.allocate(10 * 1024)
//      val tmpFile = File.createTempFile("keyscore", null)
//      val output = new FileOutputStream(tmpFile)
//      val input = new FileInputStream(tmpFile)
//
//      dataset.writeTo(output)
//
//      val parsedDataset = NativeDataset.parseFrom(input)
//
//      parsedDataset shouldBe dataset
//
//      buffer.flip()
//
//      val parsedTextField: TextField = NativeField.parseFrom(buffer)
//      val anotherTextField = textField
//
//      parsedTextField shouldBe anotherTextField
//    }
//  }

  def newOutputStream(buffer: ByteBuffer): OutputStream = new OutputStream() {

    override def write(bytes: Array[Byte], offset: Int, length: Int): Unit = buffer.put(bytes, offset, length)

    override def write(b: Int): Unit = buffer.put(b.asInstanceOf[Byte])
  }
}