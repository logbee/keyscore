package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import java.nio.file.Path


abstract class WatcherProvider() {

  def createFileWatcher(file: FileHandle): FileWatcher

  def createDirWatcher(dirPath: Path, matchPattern: DirWatcherPattern): DirWatcher
}
