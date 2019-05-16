package io.logbee.keyscore.pipeline.contrib.tailin.file

trait DirHandle extends PathHandle {
  
  def listDirsAndFiles: (Seq[DirHandle], Seq[FileHandle])
  
  def equals(that: Any): Boolean
}
