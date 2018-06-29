package io.logbee.keyscore.model

import java.io.OutputStream
import java.nio.ByteBuffer

class BufferAndStream {

  val buffer = ByteBuffer.allocate(10 * 1024)
  val stream = new OutputStream() {

    override def write(bytes: Array[Byte], offset: Int, length: Int): Unit = buffer.put(bytes, offset, length)

    override def write(b: Int): Unit = buffer.put(b.asInstanceOf[Byte])
  }

  val tuple = (buffer, stream)
}
