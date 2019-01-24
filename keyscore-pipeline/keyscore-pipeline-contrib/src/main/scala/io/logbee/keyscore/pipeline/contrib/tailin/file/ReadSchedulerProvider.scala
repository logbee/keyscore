package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule


class ReadSchedulerProvider(rotationPattern: String, persistenceContext: PersistenceContext) extends WatcherProvider {
  
  val readSchedule = new ReadSchedule()
  
  def createDirWatcher(configuration: DirWatcherConfiguration): DirWatcher = {
    new DefaultDirWatcher(configuration, this)
  }
  
  def createFileWatcher(file: File): FileWatcher = {
    new ReadScheduler(file, rotationPattern, persistenceContext, readSchedule)
  }
}
