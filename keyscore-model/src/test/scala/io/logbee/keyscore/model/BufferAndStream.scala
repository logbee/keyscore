package io.logbee.keyscore.model

import java.io.{InputStream, OutputStream}
import java.nio.ByteBuffer

class BufferAndStream {

  val buffer = ByteBuffer.allocate(10 * 1024)
  val output = new OutputStream() {

    override def write(bytes: Array[Byte], offset: Int, length: Int): Unit = buffer.put(bytes, offset, length)

    override def write(b: Int): Unit = buffer.put(b.asInstanceOf[Byte])
  }

  val input = new InputStream {

    override def read(): Int = {
      if (!buffer.hasRemaining) {
        return -1
      }

      buffer.get() & 0xFF
    }

    override def read(bytes: Array[Byte], offset: Int, length: Int): Int = {
      if (!buffer.hasRemaining) {
        return -1
      }

      val minLength = Math.min(length, buffer.remaining())
      buffer.get(bytes, offset, minLength)

      minLength
    }
  }

  val tuple = (buffer, input, output)
}
