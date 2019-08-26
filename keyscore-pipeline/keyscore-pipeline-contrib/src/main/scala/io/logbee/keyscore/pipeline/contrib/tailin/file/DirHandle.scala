package io.logbee.keyscore.pipeline.contrib.tailin.file

trait DirHandle extends PathHandle {
  
  def listDirsAndFiles: (Set[_ <: DirHandle], Set[_ <: FileHandle])
  
  def getDirChangeListener(): DirChangeListener
  
  def equals(that: Any): Boolean
}
