package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File

import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule


class ReadSchedulerProvider(rotationPattern: String, persistenceContext: PersistenceContext, callback: String => Unit) extends WatcherProvider {
  
  private val changelogFile = new File(".keyscoreTailinReadSchedule")
  val readSchedule = new ReadSchedule(changelogFile)
  
  def createDirWatcher(configuration: DirWatcherConfiguration): DirWatcher = {
    new DefaultDirWatcher(configuration, this, callback)
  }
  
  def createFileWatcher(file: File): FileWatcher = {
    new ReadScheduler(file, rotationPattern, persistenceContext, readSchedule)
  }
}
