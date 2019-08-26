package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer

trait FileHandle extends PathHandle {
  val name: String
  
  val parent: String
  
  def listRotatedFiles(rotationPattern: String): Seq[_ <: FileHandle]
  
  def length: Long
  
  def lastModified: Long
  
  def read(buffer: ByteBuffer, offset: Long): Int
  
  def delete(): Unit
  
  def move(newPath: String): Unit
}
