package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.nio.file.Path

import io.logbee.keyscore.pipeline.contrib.tailin.file.LocalFile
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule


class LocalWatcherProvider(readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) extends WatcherProvider[Path, LocalFile] {
  
  def createDirWatcher(watchDir: Path, matchPattern: DirWatcherPattern): DirWatcher = {
    new LocalDirWatcher(watchDir, matchPattern, this)
  }
  
  def createFileEventHandler(file: LocalFile): FileEventHandler = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
