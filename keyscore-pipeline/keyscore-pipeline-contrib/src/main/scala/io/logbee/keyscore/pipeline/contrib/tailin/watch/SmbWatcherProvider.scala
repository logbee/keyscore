package io.logbee.keyscore.pipeline.contrib.tailin.watch

import com.hierynomus.smbj.share.Directory

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadPersistence
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.ReadSchedule

class SmbWatcherProvider(readSchedule: ReadSchedule, rotationPattern: String, readPersistence: ReadPersistence) extends WatcherProvider[Directory] {
  
  def createDirWatcher(dirPath: Directory, matchPattern: DirWatcherPattern): DirWatcher = {
    new SmbDirWatcher(dirPath, matchPattern, this)
  }

  def createFileEventHandler(file: FileHandle): FileEventHandler = {
    new ReadScheduler(file, rotationPattern, readPersistence, readSchedule)
  }
}
