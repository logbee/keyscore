package io.logbee.keyscore.pipeline.contrib.tailin.file

case class DirChanges(
  newlyCreatedDirs: Set[_ <: DirHandle],
  newlyCreatedFiles: Set[_ <: FileHandle],
  deletedPaths: Set[_ <: PathHandle],
  potentiallyModifiedDirs: Set[_ <: DirHandle],
  potentiallyModifiedFiles: Set[_ <: FileHandle],
)

abstract class DirChangeListener(dir: DirHandle) {
  def getChanges: DirChanges
  
  def tearDown(): Unit
}
