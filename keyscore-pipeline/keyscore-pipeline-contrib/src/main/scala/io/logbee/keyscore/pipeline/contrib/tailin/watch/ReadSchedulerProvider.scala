package io.logbee.keyscore.pipeline.contrib.tailin.watch

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule


class ReadSchedulerProvider(readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) extends WatcherProvider {
  
  def createDirWatcher(configuration: DirWatcherConfiguration): DirWatcher = {
    new DefaultDirWatcher(configuration, this)
  }
  
  def createFileWatcher(file: File): FileWatcher = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
