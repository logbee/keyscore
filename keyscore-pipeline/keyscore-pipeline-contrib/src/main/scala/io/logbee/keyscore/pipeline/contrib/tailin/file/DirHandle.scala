package io.logbee.keyscore.pipeline.contrib.tailin.file

import io.logbee.keyscore.pipeline.contrib.tailin.watch.DirChanges

trait DirHandle extends PathHandle {
  
  def listDirsAndFiles: (Set[_ <: DirHandle], Set[_ <: FileHandle])
  
  def getChanges: DirChanges
  
  def equals(that: Any): Boolean
}
