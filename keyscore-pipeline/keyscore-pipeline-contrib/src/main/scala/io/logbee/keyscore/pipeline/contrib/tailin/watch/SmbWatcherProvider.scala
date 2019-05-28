package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.file.DirHandle

class SmbWatcherProvider(readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) extends WatcherProvider[DirHandle, FileHandle] {
  
  def createDirWatcher(watchDir: DirHandle, matchPattern: DirWatcherPattern): DirWatcher = {
    new SmbDirWatcher(watchDir, matchPattern, this)
  }

  def createFileEventHandler(file: FileHandle): FileEventHandler = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
