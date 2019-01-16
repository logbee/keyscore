package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File
import java.nio.charset.Charset

import io.logbee.keyscore.pipeline.contrib.tailin.file.ReadMode.ReadMode
import io.logbee.keyscore.pipeline.contrib.tailin.persistence.PersistenceContext

class RotationReaderProvider(rotationPattern: String, persistenceContext: PersistenceContext, bufferSize: Int, callback: String => Unit, charset: Charset, readMode: ReadMode) extends WatcherProvider() {

  def createDirWatcher(configuration: DirWatcherConfiguration): DefaultDirWatcher = {
    new DefaultDirWatcher(configuration, this, callback)
  }

  def createFileWatcher(file: File): FileWatcher = {
    new FileReader(file, rotationPattern, persistenceContext, bufferSize, charset, readMode)
  }
}
