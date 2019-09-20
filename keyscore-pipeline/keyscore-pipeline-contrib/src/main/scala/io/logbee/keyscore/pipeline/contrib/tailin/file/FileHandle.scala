package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.nio.ByteBuffer

import scala.util.Try

case class FileNotOpenableException(message: String, throwable: Throwable) extends RuntimeException(message, throwable)
case class DeleteFileFailedException(message: String, cause: Throwable) extends RuntimeException(message, cause)
case class MoveFileFailedException(message: String, cause: Throwable) extends RuntimeException(message, cause)

trait FileHandle extends PathHandle {
  def open[T](file: Try[OpenFileHandle] => T): T
  
  def delete(): Try[Unit]
  
  def move(newPath: String): Try[Unit]
}

trait OpenFileHandle extends OpenPathHandle {
  def name: String
  
  def parent: String
  
  def listRotatedFiles(rotationPattern: String): Seq[_ <: FileHandle]
  
  def length: Long
  
  def lastModified: Long
  
  def read(buffer: ByteBuffer, offset: Long): Int
}
