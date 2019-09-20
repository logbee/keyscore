package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle

class WatcherProvider[D <: DirHandle[D, F], F <: FileHandle](readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) {
  
  def createDirWatcher(watchDir: DirHandle[D, F], matchPattern: FileMatchPattern[D, F]): BaseDirWatcher = {
    new DirWatcher(watchDir, matchPattern, this)
  }

  def createFileEventHandler(file: FileHandle): FileEventHandler = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
