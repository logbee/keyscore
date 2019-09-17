package io.logbee.keyscore.pipeline.contrib.tailin.file

case class DirChanges[D <: DirHandle[D, F], F <: FileHandle](
   newlyCreatedDirs: Seq[D],
   newlyCreatedFiles: Seq[F],
   deletedPaths: Seq[_ <: PathHandle],
   potentiallyModifiedDirs: Seq[D],
   potentiallyModifiedFiles: Seq[F],
)

abstract class DirChangeListener[D <: DirHandle[D, F],
                                 F <: FileHandle]
                                 (dir: D) {
  def getChanges: DirChanges[D, F]
  
  def tearDown(): Unit
}
