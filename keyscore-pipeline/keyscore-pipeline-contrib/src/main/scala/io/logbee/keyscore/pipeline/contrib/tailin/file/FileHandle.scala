package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer

trait FileHandle {
  def name: String
  
  def absolutePath: String
  
  def listRotatedFiles(rotationPattern: String): Seq[FileHandle]
  
  def length: Long
  
  def lastModified: Long
  
  def read(buffer: ByteBuffer, offset: Long): Int
  
  def tearDown()
}