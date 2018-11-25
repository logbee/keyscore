package io.logbee.keyscore.contrib.tailin

import java.nio.file.FileSystems
import java.io.File
import io.logbee.keyscore.contrib.tailin.persistence.FilePersistenceContext
import io.logbee.keyscore.contrib.tailin.persistence.PersistenceContext
import io.logbee.keyscore.contrib.tailin.ReadMode._
import java.nio.charset.Charset

class RotationReaderProvider(rotationSuffix: String, persistenceContext: PersistenceContext, bufferSize: Int, callback: (String) => Unit, charset: Charset, readMode: ReadMode) extends WatcherProvider() {

  def createDirWatcher(configuration: DirWatcherConfiguration): DefaultDirWatcher = {
    new DefaultDirWatcher(configuration, this, callback)
  }

  def createFileWatcher(file: File): FileWatcher = {
    new FileReader(file, rotationSuffix, persistenceContext, bufferSize, charset, readMode)
  }
}
