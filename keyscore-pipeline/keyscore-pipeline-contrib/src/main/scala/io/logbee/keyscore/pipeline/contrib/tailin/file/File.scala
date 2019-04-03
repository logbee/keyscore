package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer

trait File {
  def name: String
  
  def absolutePath: String
  
  def length: Long
  
  def lastModified: Long
  
  def read(buffer: ByteBuffer, offset: Long): Int
}