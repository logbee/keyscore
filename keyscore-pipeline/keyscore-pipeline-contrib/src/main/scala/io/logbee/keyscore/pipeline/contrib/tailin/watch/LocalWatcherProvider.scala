package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.Path

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule


class LocalWatcherProvider(readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) extends WatcherProvider[Path] {
  
  def createDirWatcher(dirPath: Path, matchPattern: DirWatcherPattern): DirWatcher = {
    new LocalDirWatcher(dirPath, matchPattern, this)
  }
  
  def createFileEventHandler(file: FileHandle): FileEventHandler = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
