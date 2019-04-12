package io.logbee.keyscore.pipeline.contrib.tailin.watch

import io.logbee.keyscore.pipeline.contrib.tailin.file.FileHandle
import java.nio.file.Path


trait WatcherProvider {
  
	def createDirWatcher(dirPath: Path, matchPattern: DirWatcherPattern): DirWatcher
  
	def createFileEventHandler(file: FileHandle): FileEventHandler
}
