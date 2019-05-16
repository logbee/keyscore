package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.SmbDir
import io.logbee.keyscore.pipeline.contrib.tailin.file.SmbFile
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule

class SmbWatcherProvider(readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) extends WatcherProvider[SmbDir, SmbFile] {
  
  def createDirWatcher(watchDir: SmbDir, matchPattern: DirWatcherPattern): DirWatcher = {
    new SmbDirWatcher(watchDir, matchPattern, this)
  }

  def createFileEventHandler(file: SmbFile): FileEventHandler = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
