package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule
import java.nio.file.Path


class LocalWatcherProvider(readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) extends WatcherProvider {
  
  def createDirWatcher(dirPath: Path, matchPattern: DirWatcherPattern): DirWatcher = {
    new LocalDirWatcher(dirPath, matchPattern, this)
  }
  
  def createFileWatcher(file: FileHandle): FileWatcher = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
