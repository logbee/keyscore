package io.logbee.keyscore.pipeline.contrib.tailin.read

import java.nio.charset.Charset

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.read.PostReadFileAction.PostReadFileActionFunc

class FileReaderProvider(byteBufferSize: Int, charset: Charset, readMode: ReadMode, postReadFileActionFunc: PostReadFileActionFunc)  {
  
  def create(file: FileHandle): FileReader = {
    new FileReader(file, byteBufferSize, charset, readMode, postReadFileActionFunc)
  }
}
