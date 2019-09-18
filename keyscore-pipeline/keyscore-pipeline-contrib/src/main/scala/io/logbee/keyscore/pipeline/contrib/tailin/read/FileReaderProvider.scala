package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.Charset

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle

class FileReaderProvider(rotationPattern: String, byteBufferSize: Int, charset: Charset, readMode: ReadMode, firstLinePattern: String, fileCompleteActions: Seq[FileHandle => Unit])  {
  
  def create(file: FileHandle): FileReader = {
    new FileReader(file, rotationPattern, byteBufferSize, charset, readMode, firstLinePattern, fileCompleteActions)
  }
}
