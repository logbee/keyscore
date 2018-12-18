package io.logbee.keyscore.pipeline.contrib.tailin.file

import java.io.File


abstract class WatcherProvider() {

  def createFileWatcher(file: File): FileWatcher

  def createDirWatcher(configuration: DirWatcherConfiguration): DirWatcher
}
