package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle


abstract class WatcherProvider() {

  def createFileWatcher(file: FileHandle): FileWatcher

  def createDirWatcher(configuration: DirWatcherConfiguration): DirWatcher
}
