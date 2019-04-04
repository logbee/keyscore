package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.File


abstract class WatcherProvider() {

  def createFileWatcher(file: File): FileWatcher

  def createDirWatcher(configuration: DirWatcherConfiguration): DirWatcher
}
