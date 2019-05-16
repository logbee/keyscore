package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer

trait FileHandle extends PathHandle {
  def name: String
  
  def listRotatedFiles(rotationPattern: String): Seq[FileHandle]
  
  def length: Long
  
  def lastModified: Long
  
  def read(buffer: ByteBuffer, offset: Long): Int
}
