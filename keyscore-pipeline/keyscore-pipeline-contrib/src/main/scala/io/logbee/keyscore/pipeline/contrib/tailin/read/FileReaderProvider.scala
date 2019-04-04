package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.Charset

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.read.ReadMode.ReadMode

class FileReaderProvider(rotationPattern: String, byteBufferSize: Int, charset: Charset, readMode: ReadMode)  {
  
  def create(file: FileHandle): FileReader = {
    new FileReader(file, rotationPattern, byteBufferSize, charset, readMode)
  }
}
