package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.channels.FileChannel

class LocalFile(file: java.io.File) extends File {
  
  def name: String = {
    file.getName
  }
  
  def absolutePath: String = {
    file.getAbsolutePath
  }
  
  def length: Long = {
    file.length
  }
  
  def lastModified: Long = {
    file.lastModified
  }
  
  
  private val fileReadChannel = Files.newByteChannel(file.toPath, StandardOpenOption.READ).asInstanceOf[FileChannel]
  
  def read(buffer: ByteBuffer, offset: Long): Int = {
    fileReadChannel.read(buffer, offset)
  }
}