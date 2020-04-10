package io.logbee.keyscore.pipeline.contrib.tailin.file

import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChangeListener

import scala.util.Try

case class DirNotOpenableException(message: String, throwable: Throwable) extends RuntimeException(message, throwable)

trait DirHandle[D <: DirHandle[D, F], F <: FileHandle] extends PathHandle {
  @throws[DirNotOpenableException]
  def open[T](dir: Try[OpenDirHandle[D, F]] => T): T
  
  def getDirChangeListener(): DirChangeListener[D, F]

  def toString: String
}

trait OpenDirHandle[D <: DirHandle[D, F], F <: FileHandle] extends OpenPathHandle {
  
  def listDirsAndFiles: (Seq[D], Seq[F])
  
  def equals(that: Any): Boolean
}
